package integration;


import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.service.message.MessageService;
import me.teixayo.bytetalk.backend.service.message.MongoMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Testcontainers
public class MessagesTest {

    @Container
    public MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeEach
    public void setUp() {
        mongoDBContainer.start();
        String connectionString = mongoDBContainer.getConnectionString();
        new MongoDBConnection(connectionString);
    }

    @Test
    public void testMessages() {
        MessageService messageService = new MongoMessageService();
        double avgDelay = 0;
        for (int i = 0; i < 100; i++) {
            Message testTestTestSetset = new Message(ThreadLocalRandom.current().nextInt(), ThreadLocalRandom.current().nextInt(),
                    "Test test test setset" + i, Date.from(Instant.now()));
            long start = System.nanoTime();
            messageService.saveMessage(testTestTestSetset);
            long end = System.nanoTime();
            log.info("{} ns | {}", end - start, i);
            avgDelay += end - start;
        }
        avgDelay /= 100;

        log.info("Avg Delay: " + avgDelay);

        long start = System.nanoTime();
        List<Message> messages = messageService.loadMessagesBeforeDate(Date.from(Instant.now()), 100);
        long end = System.nanoTime();
        log.info("{} ns", end - start);


        for (Message message : messages) {
            log.info("Message: {}", message.getContent());
        }
    }
}