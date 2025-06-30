package me.teixayo.bytetalk.backend.database.redis;

import me.teixayo.bytetalk.backend.Server;

public enum RedisKeys {


    MESSAGES("messages:%s"),
    MESSAGES_LIST("messages_list");

    private final String template;

    private RedisKeys(String template) {
        this.template = template;
    }

    public String getKey(Object... replacements) {
        return Server.getInstance().getConfig().getRedisPrefix() + ':' + String.format(this.template, replacements);
    }

}
