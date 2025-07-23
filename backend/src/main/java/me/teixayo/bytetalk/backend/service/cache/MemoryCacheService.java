package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.service.message.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MemoryCacheService implements CacheService {

    private final int maxSize;
    private final HashMap<Long, ConcurrentLinkedDeque<Message>> messageCache;

    public MemoryCacheService(int maxSize) {
        messageCache = new HashMap<>();
        this.maxSize = maxSize;
    }

    public MemoryCacheService() {
        this(10);
    }

    @Override
    public Collection<Message> loadLastestMessages(long channelID) {
        ConcurrentLinkedDeque<Message> messages = messageCache.get(channelID);
        if (messages == null) return List.of();
        return messages;
    }


    @Override
    public void addMessageToCache(long channelID, Message message) {
        ConcurrentLinkedDeque<Message> currentCache = messageCache.get(channelID);
        if (currentCache == null) {
            currentCache = new ConcurrentLinkedDeque<>();
            messageCache.put(channelID, currentCache);
        }
        while (currentCache.size() >= maxSize) {
            currentCache.removeFirst();
        }
        currentCache.addLast(message);
    }

    @Override
    public void addMessagesToCache(long channelID, List<Message> messageList) {
        for (Message message : messageList) {
            addMessageToCache(channelID, message);
        }
    }
}