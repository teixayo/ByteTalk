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

//    private static String REDIS_ADDRESS = System.getenv("REDIS_ADDRESS");
//    private static final String REDIS_PORT = System.getenv("REDIS_PORT");
//    private static final String REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");
    public RedisDBConnection(int port) {
        String REDIS_ADDRESS = "localhost";
        String REDIS_PORT= String.valueOf(port);
        String REDIS_PASSWORD = "";
        String REDIS_USER="root";

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
        jedisPool = new JedisPool(config, REDIS_ADDRESS, Integer.parseInt(REDIS_PORT), 2000);

        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            if ("PONG".equals(response)) {
                log.info("Redis is connected successfully");
            } else {
                log.error("Redis ping returned: {}", response);
            }
        } catch (Exception e) {
            log.error("Failed to connect to RedisDB: ", e);
        }
    }

    public static void start() {
        if (instance == null) {
            new RedisDBConnection(6379);
        }
    }

    public static void stop() {
        if (instance != null) {
            jedisPool.close();
        }
    }
}