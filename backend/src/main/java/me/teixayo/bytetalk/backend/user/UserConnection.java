package me.teixayo.bytetalk.backend.user;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import lombok.Getter;
import lombok.SneakyThrows;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;

import java.util.concurrent.ConcurrentLinkedQueue;

public class UserConnection {


    @Getter
    private final ChannelHandlerContext channel;
    private final ConcurrentLinkedQueue<ServerPacket> serverPackets = new ConcurrentLinkedQueue<>();
    private boolean online = true;

    public UserConnection(ChannelHandlerContext channel) {
        this.channel = channel;
    }


    public void sendPacket(ServerPacket serverPacket) {
        serverPackets.add(serverPacket);
    }

    @SneakyThrows
    public void disconnect() {
        channel.writeAndFlush(new CloseWebSocketFrame());
        channel.channel().closeFuture().addListener((ChannelFutureListener) future -> {
            online = false;
        });

    }

    public void processPackets() {
        if (channel.isRemoved()) return;
        boolean hasPacket = !serverPackets.isEmpty();
        while (!serverPackets.isEmpty() && channel.channel().isWritable()) {
            ServerPacket serverPacket = serverPackets.poll();
            if (serverPacket == null) continue;
            ChannelFuture channelFuture = channel.write(serverPacket);
            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
        if (hasPacket) channel.flush();
    }

    public void handleClientPacket(ClientPacket packet) {

    }
}
