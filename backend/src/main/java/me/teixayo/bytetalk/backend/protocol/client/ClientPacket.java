package me.teixayo.bytetalk.backend.protocol.client;

import lombok.Getter;
import org.json.JSONObject;

@Getter
public class ClientPacket {

    private ClientPacketType packetType;
    private JSONObject data;
    public ClientPacket(ClientPacketType packetType, JSONObject data) {
        this.packetType = packetType;
        this.data = data;
    }
}
