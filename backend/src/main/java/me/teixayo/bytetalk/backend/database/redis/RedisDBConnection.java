package me.teixayo.bytetalk.backend.database.redis;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;

public class RedisDBConnection extends JedisPubSub
{

    @Getter
    private static RedisDBConnection instance;

    @Getter
    private static JedisPool jedisPool;

//    private static String REDIS_ADDRESS = System.getenv("REDIS_ADDRESS");
//    private static final String REDIS_PORT = System.getenv("REDIS_PORT");
//    private static final String REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");

    private static Logger LOGGER = LoggerFactory.getLogger(RedisDBConnection.class);

    public RedisDBConnection() {
        String REDIS_ADDRESS = "localhost";
        String REDIS_PORT="6379";
        String REDIS_PASSWORD = "";



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
                LOGGER.info("Redis is connected successfully");
            } else {
                LOGGER.error("Redis ping returned: {}", response);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to connect to Redis: {}", e.getMessage());
        }
    }

    public static void start() {
        if (instance == null) {
            new RedisDBConnection();
        }
    }

    public static void stop() {
        if (instance != null) {
            jedisPool.close();
        }
    }
}