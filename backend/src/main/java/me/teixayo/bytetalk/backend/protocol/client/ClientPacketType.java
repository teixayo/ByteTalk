package me.teixayo.bytetalk.backend.protocol.client;

import org.json.JSONObject;

public enum ClientPacketType {
    CreateUser,
    Login,
    SendMessage,
    RequestBulkMessage,
    WritingMessage,
    Messaging,
    CanSendMessage;

    public ClientPacket createPacket(Object... fields) {
        JSONObject jsonObject = new JSONObject();
        int length = fields.length;
        for (int i = 0; i < length / 2; i++) {
            jsonObject.put((String) (fields[2 * i]), fields[2 * i + 1]);
        }

        jsonObject.put("type", name());
        return new ClientPacket(this, jsonObject);
    }

    public ClientPacket createPacket(JSONObject jsonObject) {
        return new ClientPacket(this, jsonObject);
    }
}
