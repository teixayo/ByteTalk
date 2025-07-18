package me.teixayo.bytetalk.backend.service.channel;

import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;

import java.util.Date;
import java.util.List;

public interface ChannelService {

    static ChannelService findBestService() {
        if (MongoDBConnection.isConnected()) return new MongoChannelService();
        return new MemoryChannelService();
    }

    void createChannel(Channel channel);
    Channel getChannel(long channelId);
    Channel getChannelByName(String name);
    void saveMessage(long channelId, long messageId, Date date);

    List<Channel> getUserPrivateChannels(long userId);

    List<Long> loadMessagesBeforeDate(long channelId, Date date, int batchSize);

}