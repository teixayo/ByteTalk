package me.teixayo.bytetalk.backend.user;

import lombok.Getter;
import lombok.Setter;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;

@Getter
public class User {
    private String name;
    private String password;
    @Setter
    private UserConnection userConnection;
    private long id;

    public User(long id, String name, String password,UserConnection userConnection) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.userConnection = userConnection;

    }
    public void sendPacket(ServerPacket serverPacket) {
        userConnection.sendPacket(serverPacket);
    }

}

