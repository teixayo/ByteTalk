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
        Channel channel = new Channel(10, "Test", Date.from(Instant.ofEpochMilli(1000)), List.of(10L,1L), false);
        Channel channel1 = new Channel(11, "Test1", Date.from(Instant.ofEpochMilli(2000)), List.of(10L,2L), false);
        Channel channel2 = new Channel(12, "Test2", Date.from(Instant.ofEpochMilli(3000)), List.of(10L,3L), false);
        Channel channel3 = new Channel(13, "Test3", Date.from(Instant.ofEpochMilli(4000)), List.of(11L,4L), false);

        channelService.createChannel(channel1);
        channelService.createChannel(channel2);
        channelService.createChannel(channel3);
        channelService.createChannel(channel);

        channelService.saveMessage(channel.getId(),10,Date.from(Instant.ofEpochMilli(1000)));
        channelService.saveMessage(channel1.getId(),11,Date.from(Instant.ofEpochMilli(2000)));
        channelService.saveMessage(channel2.getId(),12,Date.from(Instant.ofEpochMilli(3000)));

        List<Channel> userPrivateChannels = channelService.getUserPrivateChannels(10);
        for (Channel pvChannels : userPrivateChannels) {
            log.info(pvChannels.getName());
        }

//        userPrivateChannels = channelService.getUserPrivateChannels(4);
//        for (Channel pvChannels : userPrivateChannels) {
//            log.info(pvChannels.getName());
//        }


//
//        channelService.saveMessage(10,1,Date.from(Instant.ofEpochMilli(1752780262244L)));
//        channelService.saveMessage(10,2,Date.from(Instant.ofEpochMilli(1752780263244L)));
//        channelService.saveMessage(10,3,Date.from(Instant.ofEpochMilli(1752780264244L)));
//
//        List<Long> ids = channelService.loadMessagesBeforeDate(10, Date.from(Instant.ofEpochMilli(1752780262243L)), 100);
//
//        for (Long id : ids) {
//            log.info(String.valueOf(id));
//        }



    }
}
