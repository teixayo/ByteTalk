package me.teixayo.bytetalk.backend.service.channel;

import java.util.Date;
import java.util.List;

public interface ChannelService {

    void createChannel(Channel channel);
    Channel getChannel(long channelId);
    void saveMessage(long channelId, long messageId, Date date);

    List<Long> loadMessagesBeforeDate(long channelId, Date date, int batchSize);

}
