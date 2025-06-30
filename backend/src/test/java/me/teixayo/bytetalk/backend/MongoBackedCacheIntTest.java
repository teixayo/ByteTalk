package me.teixayo.bytetalk.backend;

import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.security.EncryptionUtils;
import me.teixayo.bytetalk.backend.service.user.MongoUserService;
import me.teixayo.bytetalk.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.*;

@Slf4j
@Testcontainers
public class MongoBackedCacheIntTest {

    @Container
    public MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeEach
    public void setUp() {
        mongoDBContainer.start();
        String connectionString = mongoDBContainer.getConnectionString();
        new MongoDBConnection(connectionString);
        MongoUserService mongoUserService = new MongoUserService();
        String name = "Test";
        String password = EncryptionUtils.encrypt("MyPassword");
        long id = mongoUserService.saveUser(name, password);

        assertUserEquals(mongoUserService.getUserByUserName(name), name, password, id);
        assertUserEquals(mongoUserService.getUserById(id), name, password, id);

        assertEquals(password, mongoUserService.getPasswordByUser(name));
        assertTrue(mongoUserService.isUserExists(id));
        assertTrue(mongoUserService.isUserExists(name));
        assertFalse(mongoUserService.isUserExists(-1));
        assertFalse(mongoUserService.isUserExists("-"));
    }
    private void assertUserEquals(User user, String expectedName, String expectedPassword, long expectedId) {
        assertEquals(expectedName, user.getName());
        assertEquals(expectedPassword, user.getPassword());
        assertEquals(expectedId, user.getId());
    }
}
