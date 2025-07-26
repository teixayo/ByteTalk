package me.teixayo.bytetalk.backend.protocol.client;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ClientPacketTest {

    @Test
    public void testConstruction() {
        ClientPacketType packetType = ClientPacketType.Login;
        JSONObject data = new JSONObject();

        ClientPacket packet = new ClientPacket(packetType, data);
        assertEquals(packetType, packet.getPacketType());
        assertEquals(data, packet.getData());

    }

    @Test
    public void testCreatePacket() {
        ClientPacketType packetType = ClientPacketType.CreateUser;

        ClientPacket packet = packetType.createPacket(
                "data", "test",
                "data1", "test1");
        assertEquals(packetType, packet.getPacketType());
        assertEquals("test", packet.getData().get("data"));
        assertEquals("test1", packet.getData().get("data1"));
        assertEquals(packetType.name(), packet.getData().get("type"));

        JSONObject data = new JSONObject();
        data.put("data2", "test2");
        data.put("data3", "test3");

        packet = packetType.createPacket(data);
        assertEquals(packetType, packet.getPacketType());
        assertEquals(data, packet.getData());

    }


}