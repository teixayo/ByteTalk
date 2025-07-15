package me.teixayo.bytetalk.backend.database.redis;

import me.teixayo.bytetalk.backend.Server;

public enum RedisKeys {


    MESSAGES("messages");

    private final String template;

    RedisKeys(String template) {
        this.template = template;
    }

    public String getKey(Object... replacements) {
        if (Server.getInstance() == null) {
            return String.format(this.template, replacements);
        }
        return Server.getInstance().getConfig().getRedisPrefix() + ':' + String.format(this.template, replacements);
    }

}
