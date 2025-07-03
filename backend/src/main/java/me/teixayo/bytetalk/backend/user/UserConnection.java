package me.teixayo.bytetalk.backend.user;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;
import me.teixayo.bytetalk.backend.security.RandomGenerator;

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

    public UserConnection(ChannelHandlerContext channel, User user) {
        this.channel = channel;
        this.user = user;
        lastPongTime = System.currentTimeMillis();
    }


    public void sendPacket(ServerPacket serverPacket) {
        serverPackets.add(serverPacket);
    }

    @SneakyThrows
    public void disconnect() {
        channel.channel().closeFuture().addListener((ChannelFutureListener) future -> {
            online = false;
            UserManager.getInstance().removeUser(user);
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
                Message message = new Message(RandomGenerator.generateId(), user.getId(), packet.getData().getString("content"), Date.from(Instant.now()));
                Server.getInstance().getMessageService().saveMessage(message);
                Server.getInstance().getCacheService().addMessageToCache(message);
            }
            case RequestBulkMessage -> {
                long time = packet.getData().getLong("time");
                Date date = Date.from(Instant.ofEpochMilli(time));

                List<Message> loadedMessages = Server.getInstance().getMessageService().loadMessagesBeforeDate(date, 100);
                user.sendMessages(loadedMessages);
            }
        }
    }
}
