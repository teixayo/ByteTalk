package me.teixayo.bytetalk.backend.database.redis;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

@Log4j2
@Getter
public class RedisDBConnection extends JedisPubSub {

    @Getter
    private static RedisDBConnection instance;

    @Getter
    private static JedisPool jedisPool;

    @Getter
    private static JedisPool messagingPool;


    @Getter
    private static boolean isConnected = false;

    @Getter
    private static Map<RedisChannel, List<Consumer<String>>> channelConsumers=null;
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
        jedisPool = new JedisPool(config, address, port, 2000, password, useSSL);
        messagingPool = new JedisPool(config, address, port, 2000, password,useSSL);

        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            isConnected = true;
            if ("PONG".equals(response)) {
                log.info("Redis is connected successfully");
            } else {
                log.error("Redis ping returned: {}", response);
            }
        } catch (Exception e) {
            log.error("Failed to connect to RedisDB: ", e);
        }


        channelConsumers = new HashMap<>();
        for(RedisChannel channel : RedisChannel.values()) {
            channelConsumers.put(channel, new ArrayList<>());
        }
        startSubscription();
    }

    private void startSubscription() {
        new Thread(() -> {
            try (Jedis jedis = messagingPool.getResource()) {
                String[] channelNames = Arrays.stream(RedisChannel.values())
                        .map(RedisChannel::getChannelName)
                        .toArray(String[]::new);

                jedis.subscribe(this, channelNames);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void registerConsumer(RedisChannel channel, Consumer<String> consumer) {
        channelConsumers.get(channel).add(consumer);
    }

    public void publish(RedisChannel channel, String message) {
        try (Jedis jedis = messagingPool.getResource()) {
            jedis.publish(channel.getChannelName(), message);
        }
    }

    public void onMessage(String channelName, String message) {
        RedisChannel channel = null;
        for (RedisChannel ch : RedisChannel.values()) {
            if (!ch.getChannelName().equals(channelName)) continue;
            channel = ch;
            break;
        }
        if (channel == null) return;
        for (Consumer<String> consumer : channelConsumers.get(channel)) {
            consumer.accept(message);
        }
    }

    public static void start(String address, int port, String password, boolean useSSL) {
        if (instance == null) {
            new RedisDBConnection(address, port, password, useSSL);
        }
    }

    public static void stop() {
        if (instance != null) {
            jedisPool.close();
            isConnected = false;
        }
    }
}