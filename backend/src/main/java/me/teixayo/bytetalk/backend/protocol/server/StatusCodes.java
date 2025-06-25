package me.teixayo.bytetalk.backend.protocol.server;

import lombok.Getter;

public enum StatusCodes {
    SUCCESS(1000),
    INCORRECT_USER_OR_PASSWORD(1001),
    USER_NOT_EXISTS(1003),
    USER_EXISTS(1004),
    INVALID_USER(1004),
    INVALID_PASSWORD(1005);


    @Getter
    private int statusCode;


    private StatusCodes(int statusCode) {
        this.statusCode = statusCode;
    }

    public ServerPacket createPacket() {
        return ServerPacketType.Status.createPacket(
                "code", String.valueOf(statusCode)
        );
    }
}
