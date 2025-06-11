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
        new RedisDBConnection(redisDBContainer.getMappedPort(6379));
    }

    @Test
    public void testSaveAndRetrieveUser() {
        CacheService cacheService = new RedisCacheService();
        for(int i = 0; i < 11; i++) {
            cacheService.addMessageToCache(new Message(1, 2, "Hello!", Date.from(Instant.now())));
        }
        for (Message loadLastestMessage : cacheService.loadLastestMessages()) {
            System.out.println(loadLastestMessage.getId() + " | " + loadLastestMessage.getUserID() + " | " + loadLastestMessage.getContent() + " | " + loadLastestMessage.getDate().toString());
        }
    }
}