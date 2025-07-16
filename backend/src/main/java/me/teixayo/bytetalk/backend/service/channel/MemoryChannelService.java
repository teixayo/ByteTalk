package me.teixayo.bytetalk.backend.service.channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MemoryChannelService implements ChannelService {

    private HashMap<Long,Channel> channels;
    private HashMap<Channel, List<Long>> channelMessages;

    @Override
    public void createChannel(Channel channel) {
        channels.put(channel.getId(), channel);
    }

    @Override
    public Channel getChannel(long channelId) {
        return channels.get(channelId);
    }

    @Override
    public void saveMessage(long channelId, long messageId, Date date) {
        Channel channel = getChannel(channelId);
        if(channel==null) return;
        List<Long> messages = channelMessages.get(channelId);
        if(messages==null) {
            messages = new ArrayList<>();
            channelMessages.put(channel,messages);
        }
        messages.add(messageId);
    }

    @Override
    public List<Long> loadMessagesBeforeDate(long channelId, Date date, int batchSize) {
        return List.of();
    }
}
