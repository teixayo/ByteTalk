package integration;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.service.channel.Channel;
import me.teixayo.bytetalk.backend.service.channel.ChannelService;
import me.teixayo.bytetalk.backend.service.channel.MongoChannelService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Testcontainers
public class ChannelServiceTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeAll
    public static void setup() {
        mongoDBContainer.start();
        String connectionString = mongoDBContainer.getConnectionString();
        System.out.println(connectionString);
        new MongoDBConnection(connectionString);
    }

    @Test
    public void testMongoCacheService() {
        MongoChannelService channelService = new MongoChannelService();
        testChannelService(channelService);
    }

    @SneakyThrows
    public void testChannelService(ChannelService service) {
        Channel channel = new Channel(1,"global", Date.from(Instant.now()), List.of(),true);
        service.createChannel(channel);
        assertEquals(service.getChannel(1),channel);
        service.saveMessage(channel.getId(), 1, Date.from(Instant.now()));
        Thread.sleep(10);
        service.saveMessage(channel.getId(), 2, Date.from(Instant.now()));
        Thread.sleep(10);
        service.saveMessage(channel.getId(), 3, Date.from(Instant.now()));
        Thread.sleep(10);
        service.saveMessage(channel.getId(), 4, Date.from(Instant.now()));
        Thread.sleep(10);
        service.saveMessage(channel.getId(), 5, Date.from(Instant.now()));
        List<Long> longs = service.loadMessagesBeforeDate(channel.getId(), Date.from(Instant.now()), 10);
        for (Long messageId : longs) {
            System.out.println(messageId + " id");
        }
    }
}