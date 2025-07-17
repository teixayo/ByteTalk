package integration;

import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.service.channel.Channel;
import me.teixayo.bytetalk.backend.service.channel.ChannelService;
import me.teixayo.bytetalk.backend.service.channel.MemoryChannelService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
public class ChannelServiceTest {


    @Test
    public void test() {
        ChannelService channelService = new MemoryChannelService();
        Channel channel = new Channel(10, "Test", Date.from(Instant.now()), List.of(), false);
        channelService.createChannel(channel);

        channelService.saveMessage(10,1,Date.from(Instant.ofEpochMilli(1752780262244L)));
        channelService.saveMessage(10,2,Date.from(Instant.ofEpochMilli(1752780263244L)));
        channelService.saveMessage(10,3,Date.from(Instant.ofEpochMilli(1752780264244L)));

        List<Long> ids = channelService.loadMessagesBeforeDate(10, Date.from(Instant.ofEpochMilli(1752780262243L)), 100);

        for (Long id : ids) {
            log.info(String.valueOf(id));
        }
    }
}
