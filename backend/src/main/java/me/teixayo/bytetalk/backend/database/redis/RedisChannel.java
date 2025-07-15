package me.teixayo.bytetalk.backend.database.redis;

public enum RedisChannel {
    SEND_MESSAGE("send_message");

    private final String channelName;

    RedisChannel(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}
