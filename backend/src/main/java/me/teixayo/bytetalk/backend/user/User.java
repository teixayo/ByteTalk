package me.teixayo.bytetalk.backend.user;

import lombok.Getter;
import lombok.Setter;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.service.message.Message;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@Getter
public class User {
    private final String name;
    private final String password;
    private final long id;
    @Setter
    private UserConnection userConnection;

    public User(long id, String name, String password, UserConnection userConnection) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.userConnection = userConnection;

    }

    public void sendPacket(ServerPacket serverPacket) {
        userConnection.sendPacket(serverPacket);
    }

    public void sendMessages(String channel,Collection<Message> messages) {
        JSONArray bulkJson = new JSONArray();

        HashSet<Long> userIds = new HashSet<>();
        for (Message message : messages) {
            userIds.add(message.getUserID());
        }
        HashMap<Long, String> usernameByIds = Server.getInstance().getUserService().getUsernameByIds(userIds);

        for (Message message : messages) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", message.getId());
            jsonObject.put("username", usernameByIds.get(message.getUserID()));
            jsonObject.put("content", message.getContent());
            jsonObject.put("date", message.getDate().toInstant().toEpochMilli());
            bulkJson.put(jsonObject);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channel", channel);
        jsonObject.put("messages", bulkJson);
        ServerPacket bulkMessagePacket = ServerPacketType.BulkMessages.createPacket(jsonObject);
        sendPacket(bulkMessagePacket);
    }
}

