package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.service.message.Message;

import java.util.Collection;
import java.util.List;

public interface CacheService {

    static CacheService findBestService() {
        if (RedisDBConnection.isConnected())
            return new RedisCacheService(Server.getInstance().getConfig().getCacheMessageSize());
        return new MemoryCacheService(Server.getInstance().getConfig().getCacheMessageSize());
    }


    Collection<Message> loadLastestMessages(long channelID);

    void addMessageToCache(long channelID, Message message);

    void addMessagesToCache(long channelID, List<Message> messageList);
}
