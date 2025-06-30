package me.teixayo.bytetalk.backend.protocol.server;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerPacketTest {
    @Test
    public void testConstruction() {
        ServerPacketType packetType = ServerPacketType.Status;
        JSONObject data = new JSONObject();

        ServerPacket packet = new ServerPacket(packetType, data);
        assertEquals(packetType, packet.getPacketType());
        assertEquals(data,packet.getData());

    }
    @Test
    public void testCreatePacket() {
        ServerPacketType packetType = ServerPacketType.BulkMessages;

        ServerPacket packet = packetType.createPacket(
                "data", "test",
                "data1","test1");
        assertEquals(packetType,packet.getPacketType());
        assertEquals("test",packet.getData().get("data"));
        assertEquals("test1",packet.getData().get("data1"));
        assertEquals(packetType.name(),packet.getData().get("type"));

        JSONObject data = new JSONObject();
        data.put("data2","test2");
        data.put("data3","test3");

        packet = packetType.createPacket(data);
        assertEquals(packetType,packet.getPacketType());
        assertEquals(data,packet.getData());
        assertEquals(packetType.name(),packet.getData().get("type"));

    }


}
