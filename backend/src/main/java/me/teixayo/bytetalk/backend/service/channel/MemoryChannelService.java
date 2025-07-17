package me.teixayo.bytetalk.backend.service.channel;

import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.service.message.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class MemoryChannelService implements ChannelService {

    private HashMap<Long,Channel> channels;
    private HashMap<Channel, List<Message>> channelMessages;

    public MemoryChannelService() {
        channels = new HashMap<>();
        channelMessages = new HashMap<>();

        if(getChannel(1)==null) {
            createChannel(new Channel(1,"global",Date.from(Instant.now()),List.of(),true));
        }
    }

    @Override
    public void createChannel(Channel channel) {
        channels.put(channel.getId(), channel);
        log.info("Created channel | " + channel.getId());
    }
    @Override
    public Channel getChannel(long channelId) {
        return channels.get(channelId);
    }

    @Override
    public Channel getChannelByName(String name) {
        for (Channel channel : channels.values()) {
            if(channel.getName().equals(name)) return channel;
        }
        return null;
    }

    @Override
    public void saveMessage(long channelId, long messageId, Date date) {
        Channel channel = getChannel(channelId);
        if(channel==null) return;
        List<Message> messages = channelMessages.get(channel);
        if(messages==null) {
            messages = new ArrayList<>();
            channelMessages.put(channel,messages);
        }

        messages.add(new Message(messageId,-1,"",date));
    }

    @Override
    public List<Long> loadMessagesBeforeDate(long channelId, Date date, int batchSize) {
        Channel channel = getChannel(channelId);
        if(channel==null) return List.of();

        List<Message> messages = channelMessages.get(channel);
        if(messages==null) return List.of();
        List<Long> messagesBeforeDate = new ArrayList<>();
        for (Message message : messages) {
            if (message.getDate().after(date)) continue;
            messagesBeforeDate.add(message.getId());
        }
        return messagesBeforeDate;
    }
}
