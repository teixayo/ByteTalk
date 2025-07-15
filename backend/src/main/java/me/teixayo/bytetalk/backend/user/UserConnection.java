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
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.networking.ChannelInitializer;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import me.teixayo.bytetalk.backend.security.RateLimiter;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

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
        if(Server.getInstance()!=null) {
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
        channel.channel().attr(ChannelInitializer.getHandshake()).get().close(channel,new CloseWebSocketFrame())
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
            case SendMessage -> {
                if(!sendMessageRateLimiter.allowRequest()) return;
                String content = packet.getData().getString("content");
                if(content == null || content.isBlank() || content.length() > Server.getInstance().getConfig().getMaxSendMessageSize()) return;
                //TODO Code Status for these things
                Message message = new Message(RandomGenerator.generateId(), user.getId(), content, Date.from(Instant.now()));
                Server.getInstance().getMessageService().saveMessage(message);
                Server.getInstance().getCacheService().addMessageToCache(message);
                ServerPacket packet1 = ServerPacketType.SendMessage.createPacket(
                        "id", message.getId(),
                        "username", this.user.getName(),
                        "content", message.getContent(),
                        "date", message.getDate().toInstant().toEpochMilli()
                );

                for (User otherUser : UserManager.getInstance().getUsers().values()) {
                    if (otherUser.equals(this.user)) continue;
                    otherUser.sendPacket(packet1);
                }
                if(RedisDBConnection.isConnected()) {
                    RedisDBConnection.getInstance().publish(RedisChannel.SEND_MESSAGE, String.valueOf(message.getId()));
                }
                log.info(packet1.getData().toString());

            }
            case RequestBulkMessage -> {
                long time = packet.getData().getLong("date");
                Date date = Date.from(Instant.ofEpochMilli(time));
                sendBulkMessage(date);
            }
        }
    }
    private void sendBulkMessage(Date date) {
        if(!bulkMessageRateLimiter.allowRequest()) {
            CompletableFuture.runAsync(() -> sendBulkMessage(date),CompletableFuture.delayedExecutor(bulkMessageRateLimiter.getRefillIntervalMillis(), TimeUnit.MILLISECONDS));
        } else {
            List<Message> loadedMessages = Server.getInstance().getMessageService().loadMessagesBeforeDate(date, 40);
            user.sendMessages(loadedMessages);
        }
    }
}
