package me.teixayo.bytetalk.backend.protocol.client;

import org.json.JSONObject;

public class ClientPacket {

    private ClientPacketType packetType;
    private JSONObject data;
    public ClientPacket(ClientPacketType packetType, JSONObject data) {
        this.packetType = packetType;
        this.data = data;
    }
}
