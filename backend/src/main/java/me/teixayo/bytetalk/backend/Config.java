package me.teixayo.bytetalk.backend;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.networking.TransportType;

import java.util.Map;

@Slf4j
@Getter
public class Config {

    private Map<String,Object> data;

    private boolean redisToggle;
    private String redisAddress;
    private int redisPort;
    private String redisUser;
    private String redisPassword;
    private boolean redisSSL;
    private String redisPrefix;

    private boolean mongoToggle;
    private String mongoConnectionUrl;

    private boolean elasticToggle;
    private String elasticAddress;
    private int elasticPort;
    private String elasticUser;
    private String elasticPassword;

    private String networkingIp;
    private int networkingPort;
    private boolean networkingTcpFastOpen;
    private boolean networkingTcpNoDelay;
    private TransportType networkingTransportType;
    private int networkingWriteBufferWaterMarkLow;
    private int networkingWriteBufferWaterMarkHigh;

    private int cacheMessageSize;

    public Config(Map<String, Object> data) {
        this.data = data;
        redisToggle = (boolean) get("database.redis.toggle");
        redisAddress = (String) get("database.redis.adddress");
        redisPort = (int) get("database.redis.port");
        redisUser = (String) get("database.redis.user");
        redisPassword = (String) get("database.redis.password");
        redisSSL = (boolean) get("database.redis.ssl");
        redisPrefix = (String) get("database.redis.prefix");

        mongoToggle = (boolean) get("database.mongo.toggle");
        mongoConnectionUrl = (String) get("database.mongo.connectionURL");

        elasticToggle = (boolean) get("database.elastic.toggle");
        elasticAddress = (String) get("database.elastic.address");
        elasticPort = (int) get("database.elastic.port");
        elasticUser = (String) get("database.elastic.user");
        elasticPassword = (String) get("database.elastic.password");

        networkingIp = (String) get("networking.ip");
        networkingPort = (int) get("networking.port");
        networkingTcpFastOpen = (boolean) get("networking.tcp-fast-open");
        networkingTcpNoDelay = (boolean) get("networking.tcp-no-delay");
        String transport = ((String) get("networking.transport")).toUpperCase();

        networkingTransportType = transport.equals("AUTO") ? TransportType.bestTransportType() : TransportType.valueOf(transport);

        networkingWriteBufferWaterMarkLow = (int) get("networking.write-buffer-watermark.low");
        networkingWriteBufferWaterMarkHigh = (int) get("networking.write-buffer-watermark.high");

        cacheMessageSize = (int) get("cache.message-size");

        log.info("Config Loaded");
    }

    public Object get(String path) {
        String[] parts = path.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                throw new NullPointerException(path + " doesn't exists");
            }
        }

        return current;
    }



}
