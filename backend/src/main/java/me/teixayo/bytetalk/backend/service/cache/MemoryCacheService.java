package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.message.Message;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MemoryCacheService implements CacheService {

    private static final int MAX_SIZE = 10;
    private final ConcurrentLinkedDeque<Message> messageCache;

    public MemoryCacheService() {
        messageCache = new ConcurrentLinkedDeque<>();
    }
    @Override
    public Collection<Message> loadLastestMessages() {
        return messageCache;
    }

    @Override
    public void addMessageToCache(Message message) {
        while (messageCache.size() >= MAX_SIZE) {
            messageCache.removeFirst();
        }
        messageCache.addLast(message);
    }

    @Override
    public void addMessagesToCache(List<Message> messageList) {
        for (Message message : messageList) {
            addMessageToCache(message);
        }
    }
}
