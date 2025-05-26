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
import me.teixayo.bytetalk.backend.chat.Message;
import me.teixayo.bytetalk.backend.protocol.client.ClientPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class UserConnection {


    @Getter
    private final ChannelHandlerContext channel;
    private final ConcurrentLinkedQueue<ServerPacket> serverPackets = new ConcurrentLinkedQueue<>();
    private boolean online = true;
    private User user;

    public UserConnection(ChannelHandlerContext channel,User user) {
        this.channel = channel;
        this.user = user;
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
            ChannelFuture channelFuture = channel.write(new TextWebSocketFrame(serverPacket.getData().toString()));
            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
        if (hasPacket) channel.flush();
    }

    public void handleClientPacket(ClientPacket packet) {
        log.info(packet.getData().toString());
        switch (packet.getPacketType()) {
            case SendMessage -> {
                Server.getInstance().getMessageService().saveMessage(new Message(RandomGenerator.generateId(), user.getId(), packet.getData().getString("content"), Date.from(Instant.now())));
            }
            case RequestBulkMessage -> {
                long time = packet.getData().getLong("time");
                Date date = Date.from(Instant.ofEpochMilli(time));

                List<Message> loadedMessages = Server.getInstance().getMessageService().loadMessagesBeforeDate(date,100);

                JSONArray bulkJson = new JSONArray();
                for (Message message : loadedMessages) {
                    JSONObject jsonObject = new JSONObject();
                    String messageUsername = Server.getInstance().getUserService().getUserById(message.getUserID()).getName();
                    jsonObject.put("id",message.getId());
                    jsonObject.put("username", messageUsername);
                    jsonObject.put("content",message.getContent());
                    jsonObject.put("date",date.toInstant().toEpochMilli());

                    bulkJson.put(jsonObject);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("messages", bulkJson);
                ServerPacket bulkMessagePacket = ServerPacketType.BulkMessages.createPacket(jsonObject);
                sendPacket(bulkMessagePacket);
                processPackets();
            }
        }
    }
}
