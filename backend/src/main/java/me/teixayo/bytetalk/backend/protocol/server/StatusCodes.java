package me.teixayo.bytetalk.backend.protocol.server;

import lombok.Getter;

public enum StatusCodes {
    SUCCESS_LOGIN_WITH_PASSWORD(1000),
    NOT_SUCCESS_LOGIN_WITH_PASSWORD(1001),
    SUCCESS_LOGIN_WITH_TOKEN(1002),
    NOT_SUCCESS_LOGIN_WITH_TOKEN(1003),
    SUCCESS_SIGNUP(1004),
    USER_EXISTS(1005),
    INVALID_USER(1006),
    INVALID_PASSWORD(1007),
    ;


    @Getter
    private final int statusCode;


    StatusCodes(int statusCode) {
        this.statusCode = statusCode;
    }

    public ServerPacket createPacket() {
        return ServerPacketType.Status.createPacket(
                "code", String.valueOf(statusCode)
        );
    }
}
