package me.teixayo.bytetalk.backend.protocol.server;

import lombok.Getter;
import org.json.JSONObject;

@Getter
public class ServerPacket {
    private ServerPacketType packetType;
    private JSONObject data;
    public ServerPacket(ServerPacketType packetType, JSONObject data) {
        this.packetType = packetType;
        this.data = data;
    }
}
