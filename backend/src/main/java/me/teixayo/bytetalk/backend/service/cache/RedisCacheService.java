package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisKeys;
import me.teixayo.bytetalk.backend.service.message.Message;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.resps.StreamEntry;

import java.time.Instant;
import java.util.*;

public class RedisCacheService implements CacheService {

    private final int maxSize;
    private final JedisPool jedisPool;

    public RedisCacheService(int maxSize) {
        this.jedisPool = RedisDBConnection.getJedisPool();
        this.maxSize = maxSize;
    }

    public RedisCacheService() {
        this(10);
    }

    @Override
    public Collection<Message> loadLastestMessages(long channelID) {
        List<Message> out = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            List<StreamEntry> entries =
                    jedis.xrevrange(RedisKeys.MESSAGES.getKey(channelID),StreamEntryID.MAXIMUM_ID, StreamEntryID.MINIMUM_ID, maxSize);
            Collections.reverse(entries);
            for (StreamEntry entry : entries) {
                Map<String,String> f = entry.getFields();
                long id      = Long.parseLong(f.get("id"));
                long userID  = Long.parseLong(f.get("userID"));
                String content = f.get("content");
                Instant date = Instant.parse(f.get("date"));
                out.add(new Message(id, userID, content, Date.from(date)));
            }
        }
        return out;
    }
    @Override
    public void addMessageToCache(long channelID,Message message) {
        addMessagesToCache(channelID,List.of(message));
    }

    public void addMessagesToCache(long channelID, List<Message> messages) {

        XAddParams xAddParams = new XAddParams()
                .maxLen(maxSize);
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            for (Message msg : messages) {
                Map<String,String> map = Map.of(
                        "id",      String.valueOf(msg.getId()),
                        "userID",  String.valueOf(msg.getUserID()),
                        "content", msg.getContent(),
                        "date",    msg.getDate().toInstant().toString()
                );
                pipeline.xadd(RedisKeys.MESSAGES.getKey(channelID), map, xAddParams);
            }
            pipeline.sync();
        }
    }

}
