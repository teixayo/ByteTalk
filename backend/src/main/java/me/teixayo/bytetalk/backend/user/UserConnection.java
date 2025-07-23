package me.teixayo.bytetalk.backend.user;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.database.redis.RedisChannel;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.networking.ChannelInitializer;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import me.teixayo.bytetalk.backend.protocol.server.StatusCodes;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import me.teixayo.bytetalk.backend.security.RateLimiter;
import me.teixayo.bytetalk.backend.service.channel.Channel;
import me.teixayo.bytetalk.backend.service.message.Message;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Getter
public class UserConnection {


    private final ChannelHandlerContext channel;
    private final ConcurrentLinkedQueue<ServerPacket> serverPackets = new ConcurrentLinkedQueue<>();
    private final User user;
    private boolean online = true;
    private long lastPongTime;
    private RateLimiter sendMessageRateLimiter;
    private RateLimiter bulkMessageRateLimiter;

    public UserConnection(ChannelHandlerContext channel, User user) {
        this.channel = channel;
        this.user = user;
        this.lastPongTime = System.currentTimeMillis();
        if (Server.getInstance() != null) {
            this.sendMessageRateLimiter = Server.getInstance().getConfig().getSendMessageLimiter().copy();
            this.bulkMessageRateLimiter = Server.getInstance().getConfig().getBulkMessageLimiter().copy();
        }
    }


    public void sendPacket(ServerPacket serverPacket) {
        serverPackets.add(serverPacket);
    }

    @SneakyThrows
    public void disconnect() {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.channel().remoteAddress();
        channel.channel().attr(ChannelInitializer.getHandshake()).get().close(channel, new CloseWebSocketFrame())
                .addListener((ChannelFutureListener) future -> {
                    online = false;
                    UserManager.getInstance().removeUser(user);
                    log.info("Disconnected the {} {}", user.getName(), socketAddress);
                });

    }

    public void processPackets() {
        if (channel.isRemoved()) return;
        boolean hasPacket = !serverPackets.isEmpty();
        while (!serverPackets.isEmpty() && channel.channel().isWritable()) {
            ServerPacket serverPacket = serverPackets.poll();
            if (serverPacket == null) continue;
            ChannelFuture channelFuture = channel.write(new TextWebSocketFrame(serverPacket.getData().toString()));
            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
        if (hasPacket) channel.flush();
    }

    public void keepAlive() {
        lastPongTime = System.currentTimeMillis();
    }

    public void checkTimeOut() {
        if (System.currentTimeMillis() - lastPongTime >= Server.getInstance().getConfig().getMaxTimeOut() * 1000L) {
            disconnect();
            online = false;
            log.info("Disconnected the {} (Timed Out)", user.getName());
        }

    }

    public void handleClientPacket(ClientPacket packet) {
        log.info(packet.getData().toString());
        switch (packet.getPacketType()) {
            case CanSendMessage -> {
                String channelString = packet.getData().getString("channel");
                boolean canSendMessage = false;
                if (!channelString.equals("global")) {
                    User targetUser = Server.getInstance().getUserService().getUserByUserName(channelString);
                    if (targetUser != null && !targetUser.getName().equals(user.getName())) {
                        canSendMessage = true;
                    }
                } else {
                    canSendMessage = true;
                }
                sendPacket(ServerPacketType.CanSendMessage.createPacket(
                        "status", canSendMessage,
                        "channel", channelString
                ));

            }
            case SendMessage -> {
                if (!sendMessageRateLimiter.allowRequest()) {
                    sendPacket(StatusCodes.SENT_TOO_MESSAGES.createPacket());
                    return;
                }
                String content = packet.getData().getString("content");
                if (content == null || content.isBlank() || content.length() > Server.getInstance().getConfig().getMaxSendMessageSize()) {
                    sendPacket(StatusCodes.TO_LONG_MESSAGE.createPacket());
                    return;
                }
                String channelString = packet.getData().getString("channel");
                Channel channel;
                String targetName = "global";
                if (!channelString.equals("global")) {
                    User targetUser = Server.getInstance().getUserService().getUserByUserName(channelString);
                    if (targetUser == null) {
                        return;
                    }
                    targetName = user.getName();
                    String channelName = getChannelName(targetUser.getId(), user.getId());

                    channel = Server.getInstance().getChannelService().getChannelByName(channelName);
                    if (channel == null) {
                        channel = new Channel(RandomGenerator.generateId(), channelName, Date.from(Instant.now()), List.of(targetUser.getId(), user.getId()), false);
                        Server.getInstance().getChannelService().createChannel(channel);
                    }
                } else {
                    channel = Server.getInstance().getChannelService().getChannel(1);
                }
                Message message = new Message(RandomGenerator.generateId(), user.getId(), content, Date.from(Instant.now()));
                Server.getInstance().getMessageService().saveMessage(message);
                Server.getInstance().getCacheService().addMessageToCache(channel.getId(), message);
                Server.getInstance().getChannelService().saveMessage(channel.getId(), message.getId(), message.getDate());
                ServerPacket sendMessagePacket = ServerPacketType.SendMessage.createPacket(
                        "channel", targetName,
                        "id", message.getId(),
                        "username", this.user.getName(),
                        "content", message.getContent(),
                        "date", message.getDate().toInstant().toEpochMilli()
                );

                if (channel.isGlobal()) {
                    for (User otherUser : UserManager.getInstance().getUsers().values()) {
                        otherUser.sendPacket(sendMessagePacket);
                    }
                } else {
                    for (long userId : channel.getMembers()) {
                        User otherUser = UserManager.getInstance().getUsers().get(userId);
                        if (otherUser == null) continue;
                        otherUser.sendPacket(sendMessagePacket);
                    }
                }
                if (RedisDBConnection.isConnected()) {
                    RedisDBConnection.getInstance().publish(RedisChannel.SEND_MESSAGE,
                            this.getUser().getName() + " " + channel.getId() + " " + message.getId());
                }
                log.info("{} Sent message packet", sendMessagePacket.getData().toString());

            }
            case RequestBulkMessage -> {
                long time = packet.getData().getLong("date");
                String channelString = packet.getData().getString("channel");
                Date date = time == -1 ? null : Date.from(Instant.ofEpochMilli(time));
                sendBulkMessage(channelString, date);
            }
        }
    }

    private String getChannelName(long username1, long username2) {
        if (username1 > username2) {
            long temp = username1;
            username1 = username2;
            username2 = temp;
        }
        return username1 + " " + username2;
    }

    private void sendBulkMessage(String channelName, Date date) {
        if (!bulkMessageRateLimiter.allowRequest()) return;
        Channel channel = null;
        if (channelName.equals("global")) {
            channel = Server.getInstance().getChannelService().getChannelByName(channelName);
        } else {
            User targetUser = Server.getInstance().getUserService().getUserByUserName(channelName);
            if (targetUser != null) {
                channel = Server.getInstance().getChannelService().getChannelByName(getChannelName(user.getId(), targetUser.getId()));
            }
        }

        if (channel == null) {
            user.sendMessages(channelName, List.of());
            return;
        }
        if (date == null) {
            user.sendMessages(channelName, Server.getInstance().getCacheService().loadLastestMessages(channel.getId()));
        } else {
            log.info("{} Requested messages from {}", user.getName(), channelName);
            List<Long> messageIds = Server.getInstance().getChannelService().loadMessagesBeforeDate(channel.getId(), date, 40);

            log.info("{} | {}", messageIds.size(), channel.getMembers());
            List<Message> loadedMessages = Server.getInstance().getMessageService().getMessage(messageIds);
            user.sendMessages(channelName, loadedMessages);
        }
    }
}
