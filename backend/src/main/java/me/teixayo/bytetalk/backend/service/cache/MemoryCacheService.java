package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.message.Message;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MemoryCacheService implements CacheService {

    private final int maxSize;
    private final ConcurrentLinkedDeque<Message> messageCache;

    public MemoryCacheService(int maxSize) {
        messageCache = new ConcurrentLinkedDeque<>();
        this.maxSize = maxSize;
    }

    public MemoryCacheService() {
        this(10);
    }

    @Override
    public Collection<Message> loadLastestMessages() {
        return messageCache;
    }

    @Override
    public void addMessageToCache(Message message) {
        while (messageCache.size() >= maxSize) {
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
