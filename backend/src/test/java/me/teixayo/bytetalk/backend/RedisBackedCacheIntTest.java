package me.teixayo.bytetalk.backend;


import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.service.cache.CacheService;
import me.teixayo.bytetalk.backend.service.cache.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;

@Slf4j
@Testcontainers
public class RedisBackedCacheIntTest {
    @Container
    static GenericContainer<?> redisDBContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.4"))
            .withExposedPorts(6379);
    @BeforeEach
    public void setUp() {
        redisDBContainer.start();
        new RedisDBConnection("0.0.0.0",redisDBContainer.getMappedPort(6379),"",false);
    }

    @Test
    public void testSaveAndRetrieveUser() {
        CacheService cacheService = new RedisCacheService();
        for(int i = 0; i < 5; i++) {
            long now = System.nanoTime();
            cacheService.addMessageToCache(new Message(i, i*3, "Hello!", Date.from(Instant.now())));
            long last = System.nanoTime();
            log.info("{} ns", last - now);
        }
        long now = System.nanoTime();
        Collection<Message> messages = cacheService.loadLastestMessages();
        long last = System.nanoTime();
        log.info("{} ns", last - now);

        for (Message loadLastestMessage : messages) {
            System.out.println(loadLastestMessage.getId() + " | " + loadLastestMessage.getUserID() + " | " + loadLastestMessage.getContent() + " | " + loadLastestMessage.getDate().toString());
        }
    }
}