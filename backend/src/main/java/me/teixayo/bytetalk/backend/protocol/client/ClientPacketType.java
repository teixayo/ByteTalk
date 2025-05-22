package me.teixayo.bytetalk.backend.protocol.client;

import org.json.JSONObject;

public enum ClientPacketType {
    CreateUser,
    Login,
    SendMessage;

    public ClientPacket createPacket(JSONObject jsonObject) {
        return new ClientPacket(this,jsonObject);
    }
}
