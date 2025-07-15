package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisKeys;
import me.teixayo.bytetalk.backend.message.Message;
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
    public Collection<Message> loadLastestMessages() {
        List<Message> out = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            List<StreamEntry> entries =
                    jedis.xrevrange(RedisKeys.MESSAGES.getKey(), StreamEntryID.MAXIMUM_ID, StreamEntryID.MINIMUM_ID, maxSize);
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

    public Message getMessageById(long messageId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String targetId = messageId + "-0";
            List<StreamEntry> entries = jedis.xrange(
                    RedisKeys.MESSAGES.getKey(),
                    new StreamEntryID(targetId),
                    new StreamEntryID(targetId),
                    1
            );
            if (entries.isEmpty()) {
                return null;
            }
            StreamEntry entry = entries.getFirst();
            Map<String, String> f = entry.getFields();
            long userID = Long.parseLong(f.get("userID"));
            String content = f.get("content");
            Instant date = Instant.parse(f.get("date"));
            return new Message(messageId, userID, content, Date.from(date));
        }
    }
    @Override
    public void addMessageToCache(Message message) {
        addMessagesToCache(List.of(message));
    }

    public void addMessagesToCache(List<Message> messages) {
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            for (Message msg : messages) {
                XAddParams xAddParams = new XAddParams()
                        .maxLen(maxSize)
                        .id(msg.getId() + "-0");
                Map<String,String> map = Map.of(
                        "id",      String.valueOf(msg.getId()),
                        "userID",  String.valueOf(msg.getUserID()),
                        "content", msg.getContent(),
                        "date",    msg.getDate().toInstant().toString()
                );
                pipeline.xadd(RedisKeys.MESSAGES.getKey(), map, xAddParams);
            }
            pipeline.sync();
        }
    }

}
