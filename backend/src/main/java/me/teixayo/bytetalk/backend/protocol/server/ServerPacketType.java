package me.teixayo.bytetalk.backend.protocol.server;

import org.json.JSONObject;

public enum ServerPacketType {
    Status,
    BulkMessages,
    Messaging,
    LoginToken,
    ;


    public ServerPacket createPacket(String... fields) {
        JSONObject jsonObject = new JSONObject();
        int length = fields.length;
        for (int i = 0; i < length / 2; i++) {
            jsonObject.put(fields[2 * i], fields[2 * i + 1]);
        }

        jsonObject.put("type", name());
        return new ServerPacket(this, jsonObject);
    }

    public ServerPacket createPacket(JSONObject jsonObject) {
        jsonObject.put("type", name());
        return new ServerPacket(this, jsonObject);
    }

}
