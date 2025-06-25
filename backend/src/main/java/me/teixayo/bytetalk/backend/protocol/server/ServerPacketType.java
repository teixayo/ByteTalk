package me.teixayo.bytetalk.backend.protocol.server;

import org.json.JSONObject;

public enum ServerPacketType {
    Status,
    GetToken,
    BulkMessages
    ;


    public ServerPacket createPacket(String... fields) {
        JSONObject jsonObject = new JSONObject();
        int length = fields.length;
        for(int i = 0; i < length/2; i++) {
            jsonObject.put(fields[2*i],fields[2*i+1]);
        }

        jsonObject.put("type",this);
        return new ServerPacket(this,jsonObject);
    }

    public ServerPacket createPacket(JSONObject jsonObject) {
        jsonObject.put("type",this);
        return new ServerPacket(this,jsonObject);
    }

}
