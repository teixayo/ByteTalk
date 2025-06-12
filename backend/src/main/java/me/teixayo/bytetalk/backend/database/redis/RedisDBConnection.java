package me.teixayo.bytetalk.backend.database.redis;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Log4j2
public class RedisDBConnection
{

    @Getter
    private static RedisDBConnection instance;

    @Getter
    private static JedisPool jedisPool;

    @Getter
    private static boolean isConnected = false;
    public RedisDBConnection(String address, int port, String password, boolean useSSL) {

        instance = this;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setMinIdle(2);
        config.setBlockWhenExhausted(true);
        config.setMaxWait(Duration.ofMillis(5000));
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        jedisPool = new JedisPool(config, address, port, 2000,password,useSSL);

        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            isConnected=true;
            if ("PONG".equals(response)) {
                log.info("Redis is connected successfully");
            } else {
                log.error("Redis ping returned: {}", response);
            }
        } catch (Exception e) {
            log.error("Failed to connect to RedisDB: ", e);
        }
    }

    public static void start(String address, int port, String password, boolean useSSL) {
        if (instance == null) {
            new RedisDBConnection(address,port,password,useSSL);
        }
    }

    public static void stop() {
        if (instance != null) {
            jedisPool.close();
            isConnected=false;
        }
    }
}