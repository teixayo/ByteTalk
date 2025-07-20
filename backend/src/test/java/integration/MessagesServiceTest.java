package integration;


import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.service.message.MemoryMessageService;
import me.teixayo.bytetalk.backend.service.message.Message;
import me.teixayo.bytetalk.backend.service.message.MessageService;
import me.teixayo.bytetalk.backend.service.message.MongoMessageService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Testcontainers
public class MessagesServiceTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeAll
    public static void setup() {
        mongoDBContainer.start();
        String connectionString = mongoDBContainer.getConnectionString();
        new MongoDBConnection(connectionString);
    }

    @Test
    public void testMongoMessageService() {
        MongoMessageService cacheService = new MongoMessageService();
        testMessageService(cacheService);
    }

    @Test
    public void testMemoryCacheService() {
        MemoryMessageService cacheService = new MemoryMessageService();
        testMessageService(cacheService);
    }

    public void testMessageService(MessageService service) {
        HashMap<Long,Message> messages = new HashMap<>();
        for(int i = 0; i < 100; i++) {
            Message message = new Message(i, i * 10, "HelloWorld" + i, Date.from(Instant.now()));
            service.saveMessage(message);
            messages.put(message.getId(),message);
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        if(service instanceof MongoMessageService mongoMessageService) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(mongoMessageService::finalizeAllMessages,1, TimeUnit.SECONDS);
        }
        List<Message> loadedMessages = service.getMessage(new ArrayList<>(messages.keySet()));
        for(int i = 0; i < 100; i++) {
            Message loadedMessage = loadedMessages.get(i);
            Message expectedMessage = messages.get((long)i);
            assertEquals(expectedMessage,loadedMessage);
            loadedMessage = service.getMessage(loadedMessage.getId());
            assertEquals(expectedMessage,loadedMessage);
        }
    }
}