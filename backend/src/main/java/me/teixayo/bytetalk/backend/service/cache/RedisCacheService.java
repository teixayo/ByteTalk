package me.teixayo.bytetalk.backend.service.cache;

import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisKeys;
import me.teixayo.bytetalk.backend.service.message.Message;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

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
        List<Message> result = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> ids = jedis.lrange(RedisKeys.MESSAGES_LIST.getKey(channelID), 0, -1);
            for (String idStr : ids) {
                Map<String, String> fields = jedis.hgetAll(RedisKeys.MESSAGES.getKey(channelID,idStr));
                if (fields == null || fields.isEmpty()) continue;
                long id = Long.parseLong(fields.get("id"));
                long userID = Long.parseLong(fields.get("userID"));
                String content = fields.get("content");
                String dateStr = fields.get("date");
                Date dateObj = Date.from(Instant.parse(dateStr));
                result.add(new Message(id, userID, content, dateObj));
            }
        }
        return result;
    }

    @Override
    public void addMessageToCache(long channelID,Message message) {
        addMessagesToCache(channelID,List.of(message));
    }

    public void addMessagesToCache(long channelID,List<Message> messages) {
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            for (Message msg : messages) {
                String idStr = String.valueOf(msg.getId());
                Map<String, String> map = new HashMap<>();
                map.put("id", idStr);
                map.put("userID", String.valueOf(msg.getUserID()));
                map.put("content", msg.getContent());
                map.put("date", msg.getDate().toInstant().toString());
                pipeline.hset(RedisKeys.MESSAGES.getKey(channelID,idStr), map);
                pipeline.rpush(RedisKeys.MESSAGES_LIST.getKey(channelID), idStr);
            }
            pipeline.sync();


            long listLen = jedis.llen(RedisKeys.MESSAGES_LIST.getKey(channelID));
            if (!(listLen > maxSize)) return;

            long removeCount = listLen - maxSize;
            List<String> removedIds = jedis.lrange(RedisKeys.MESSAGES_LIST.getKey(channelID), 0, removeCount - 1);
            jedis.ltrim(RedisKeys.MESSAGES_LIST.getKey(channelID), removeCount, -1);

            if (removedIds.isEmpty()) return;
            Pipeline delPipeline = jedis.pipelined();
            for (String remId : removedIds) {
                delPipeline.del(RedisKeys.MESSAGES.getKey(channelID,remId));
            }
            delPipeline.sync();
        }
    }
}
