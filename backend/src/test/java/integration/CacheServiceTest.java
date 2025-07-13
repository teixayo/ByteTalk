package integration;

import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.service.cache.CacheService;
import me.teixayo.bytetalk.backend.service.cache.MemoryCacheService;
import me.teixayo.bytetalk.backend.service.cache.RedisCacheService;
import me.teixayo.bytetalk.backend.service.message.Message;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Testcontainers
public class CacheServiceTest {
    @Container
    public static GenericContainer<?> redisDBContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.4"))
            .withExposedPorts(6379);

    @BeforeAll
    public static void setup() {
        redisDBContainer.start();
        new RedisDBConnection("0.0.0.0", redisDBContainer.getMappedPort(6379), null, false);
    }

    @Test
    public void testRedisCacheService() {
        CacheService cacheService = new RedisCacheService();
        testCacheService(cacheService);
    }

    @Test
    public void testMemoryCacheService() {
        CacheService cacheService = new MemoryCacheService();
        testCacheService(cacheService);
    }

    public void testCacheService(CacheService service) {
        List<Message> messages = new ArrayList<>();
        Date lastDate = null;
        for(int i = 0 ; i < 30; i++) {
            Message message = new Message(i, i * 10, "HelloWorld" + i, Date.from(Instant.now()));
            messages.add(message);
            lastDate = message.getDate();
        }
        service.addMessagesToCache(1,messages);
        Message[] lastestMessages = service.loadLastestMessages(1).toArray(new Message[0]);
        List<Message> last10 = messages.stream()
                .skip(Math.max(0, messages.size() - 10))
                .toList();

        for(int i = 0; i < 10; i++) {
            assertEquals(last10.get(i), lastestMessages[i]);
        }
        Message newestMessage = lastestMessages[lastestMessages.length-1];
        assertEquals(29, newestMessage.getId());
        assertEquals(290, newestMessage.getUserID());
        assertEquals("HelloWorld29", newestMessage.getContent());
        assertEquals(lastDate, newestMessage.getDate());
    }
}