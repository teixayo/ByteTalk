package me.teixayo.bytetalk.backend;

import co.elastic.clients.util.Pair;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.impl.user.MongoUserService;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.*;

@Slf4j
@Testcontainers
public class RedisBackedCacheIntTest {

    @Container
    public MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeEach
    public void setUp() {
        mongoDBContainer.start();
        String connectionString = mongoDBContainer.getConnectionString();
        new MongoDBConnection(connectionString);
    }
    @Test
    public void testSaveAndRetrieveUser() {
        MongoUserService mongoUserService = new MongoUserService();
        String name = "Test";
        Pair<String, Long> userData = mongoUserService.saveUser(name);

        String token = userData.key();
        long id = userData.value();

        assertUserEquals(mongoUserService.getUserByUserName(name), name, token, id);
        assertUserEquals(mongoUserService.getUserById(id), name, token, id);
        assertUserEquals(mongoUserService.getUserByToken(token), name, token, id);

        assertEquals(token, mongoUserService.getTokenByUser(name));
        assertTrue(mongoUserService.isUserExists(id));
        assertTrue(mongoUserService.isUserExists(name));
        assertFalse(mongoUserService.isUserExists(-1));
        assertFalse(mongoUserService.isUserExists("-"));

        assertTrue(mongoUserService.isTokenExists(token));
        assertFalse(mongoUserService.isTokenExists(""));
    }
    private void assertUserEquals(User user, String expectedName, String expectedToken, long expectedId) {
        assertEquals(expectedName, user.getName());
        assertEquals(expectedToken, user.getToken());
        assertEquals(expectedId, user.getId());
    }
}
