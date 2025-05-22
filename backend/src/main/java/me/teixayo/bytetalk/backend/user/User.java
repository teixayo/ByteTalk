package me.teixayo.bytetalk.backend.user;

import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;

public class User {
    private String name;
    private String token;
    private UserConnection userConnection;

    public User(String name, String token,UserConnection userConnection) {
        this.name = name;
        this.userConnection = userConnection;
    }

    public void sendPacket(ServerPacket serverPacket) {
        userConnection.sendPacket(serverPacket);
    }

    public String getName() {
        return name;
    }

    public UserConnection getUserConnection() {
        return userConnection;
    }

    public String getToken() {
        return token;
    }
}

