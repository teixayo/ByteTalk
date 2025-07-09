package integration;

import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.security.Crypto;
import me.teixayo.bytetalk.backend.service.user.MemoryUserService;
import me.teixayo.bytetalk.backend.service.user.MongoUserService;
import me.teixayo.bytetalk.backend.service.user.UserService;
import me.teixayo.bytetalk.backend.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.*;

@Slf4j
@Testcontainers
public class UserServiceTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeAll
    public static void setUp() {
        mongoDBContainer.start();
        String connectionString = mongoDBContainer.getConnectionString();
        new MongoDBConnection(connectionString);
    }

    @Test
    public void testMongoUserService() {
        UserService userService = new MongoUserService();
        testUserService(userService);
    }

    @Test
    public void testMemoryUserService() {
        UserService userService = new MemoryUserService();
        testUserService(userService);
    }


    public void testUserService(UserService service) {
        String name = "Test";
        String password = Crypto.encryptSHA256("MyPassword");
        long id = service.saveUser(name, password);

        assertUserEquals(service.getUserByUserName(name), name, password, id);
        assertUserEquals(service.getUserById(id), name, password, id);

        assertEquals(password, service.getPasswordByUser(name));
        assertTrue(service.isUserExists(id));
        assertTrue(service.isUserExists(name));
        assertFalse(service.isUserExists(-1));
        assertFalse(service.isUserExists("-"));
    }
    private void assertUserEquals(User user, String expectedName, String expectedPassword, long expectedId) {
        assertEquals(expectedName, user.getName());
        assertEquals(expectedPassword, user.getPassword());
        assertEquals(expectedId, user.getId());
    }
}
