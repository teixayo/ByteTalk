package integration;

import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.service.channel.Channel;
import me.teixayo.bytetalk.backend.service.channel.ChannelService;
import me.teixayo.bytetalk.backend.service.channel.MemoryChannelService;
import me.teixayo.bytetalk.backend.service.channel.MongoChannelService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class ChannelServiceTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @BeforeAll
    public static void setup() {
        mongoDBContainer.start();
        String connectionString = mongoDBContainer.getConnectionString();
        new MongoDBConnection(connectionString);
    }

    @Test
    public void testMongoChannelService() {
        ChannelService channelService = new MongoChannelService();
        testChannelService(channelService);
    }

    @Test
    public void testMemoryChannelService() {
        ChannelService channelService = new MemoryChannelService();
        testChannelService(channelService);
    }

    public void testChannelService(ChannelService service) {
        testGlobalChannel(service);
        testPrivateChannel(service);
        testChannelMessages(service);
    }

    private void testChannelMessages(ChannelService service) {
        Date date = Date.from(Instant.now());
        service.saveMessage(1, 100L, date);
        assertEquals(100L, service.loadMessagesBeforeDate(1, date, 10).getFirst());
        assertEquals(List.of(), service.loadMessagesBeforeDate(1, Date.from(date.toInstant().minusMillis(1)), 10));

        Date date1 = Date.from(Instant.now());
        service.saveMessage(1, 101L, date1);
        assertEquals(100L, service.loadMessagesBeforeDate(1, date, 10).getFirst());
        assertEquals(List.of(100L, 101L), service.loadMessagesBeforeDate(1, date1, 10));

        for (int i = 1; i <= 5; i++) {
            service.saveMessage(1, 101L + i, Date.from(Instant.now()));
        }
        assertEquals(List.of(102L, 103L, 104L, 105L, 106L), service.loadMessagesBeforeDate(1, Date.from(Instant.now()), 5));

        assertEquals(List.of(), service.loadMessagesBeforeDate(2, date, 10));
    }

    public void testPrivateChannel(ChannelService service) {
        Channel expectedChannel = new Channel(100, "test", Date.from(Instant.now()), List.of(200L, 201L), false);
        assertNull(service.getChannel(100));
        service.createChannel(expectedChannel);
        Channel channel = service.getChannel(100);
        assertNotNull(channel);
        assertEquals(expectedChannel, channel);
        assertEquals(expectedChannel, service.getChannelByName(expectedChannel.getName()));
        assertEquals(expectedChannel, service.getUserPrivateChannels(200).getFirst());
        assertEquals(expectedChannel, service.getUserPrivateChannels(201).getFirst());

        Channel expectedChannel1 = new Channel(101, "test1", Date.from(Instant.now()), List.of(200L, 201L), false);
        assertNull(service.getChannel(101));
        service.createChannel(expectedChannel1);
        channel = service.getChannel(101);
        assertNotNull(channel);
        assertEquals(expectedChannel1, channel);
        assertEquals(expectedChannel1, service.getChannelByName(expectedChannel1.getName()));
        assertEquals(List.of(expectedChannel, expectedChannel1), service.getUserPrivateChannels(200));
        assertEquals(List.of(expectedChannel, expectedChannel1), service.getUserPrivateChannels(201));
    }

    private void testGlobalChannel(ChannelService service) {
        Channel channel = service.getChannel(1L);
        assertNotNull(channel);
        assertEquals(1, channel.getId());
        assertEquals("global", channel.getName());
        assertEquals(List.of(), channel.getMembers());
        assertEquals(channel, service.getChannelByName("global"));
    }
}
