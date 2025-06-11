package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.service.message.MemoryMessageService;
import me.teixayo.bytetalk.backend.service.message.MessageService;
import me.teixayo.bytetalk.backend.service.message.MongoMessageService;

import java.util.Collection;
import java.util.List;

public interface CacheService {

    static CacheService findBestService() {
        if(!RedisDBConnection.getJedisPool().isClosed()) return new RedisCacheService();
        return new MemoryCacheService();
    }

    Collection<Message> loadLastestMessages();
    void addMessageToCache(Message message);
    void addMessagesToCache(List<Message> messageList);

}
