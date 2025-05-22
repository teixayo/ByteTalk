package me.teixayo.bytetalk.backend.protocol.server;

import org.json.JSONObject;

public class ServerPacket {
    private ServerPacketType packetType;
    private JSONObject data;
    public ServerPacket(ServerPacketType packetType, JSONObject data) {
        this.packetType = packetType;
        this.data = data;
    }
}
