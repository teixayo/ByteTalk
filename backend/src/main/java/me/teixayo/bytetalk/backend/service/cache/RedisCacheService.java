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
                    jedis.xrevrange(RedisKeys.MESSAGES.getKey(channelID), StreamEntryID.MAXIMUM_ID, StreamEntryID.MINIMUM_ID, maxSize);
            Collections.reverse(entries);
            for (StreamEntry entry : entries) {
                Map<String, String> f = entry.getFields();
                long id = Long.parseLong(f.get("id"));
                long userID = Long.parseLong(f.get("userID"));
                String content = f.get("content");
                Instant date = Instant.parse(f.get("date"));
                out.add(new Message(id, userID, content, Date.from(date)));
            }
        }
        return out;
    }

    @Override
    public void addMessageToCache(long channelID, Message message) {
        addMessagesToCache(channelID, List.of(message));
    }

    public void addMessagesToCache(long channelID, List<Message> messages) {

        XAddParams xAddParams = new XAddParams()
                .maxLen(maxSize);
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            for (Message msg : messages) {
                Map<String, String> map = Map.of(
                        "id", String.valueOf(msg.getId()),
                        "userID", String.valueOf(msg.getUserID()),
                        "content", msg.getContent(),
                        "date", msg.getDate().toInstant().toString()
                );
                pipeline.xadd(RedisKeys.MESSAGES.getKey(channelID), map, xAddParams);
            }
            pipeline.sync();
        }
    }

    public Message getMessageById(long channelID, long id) {
        String script = """
                local entries = redis.call('XRANGE', KEYS[1], '-', '+')
                for _, entry in ipairs(entries) do
                  local fields = entry[2]
                  for i = 1, #fields, 2 do
                    if fields[i] == 'id' and fields[i + 1] == ARGV[1] then
                      return entry
                    end
                  end
                end
                return nil
                """;

        try (Jedis jedis = jedisPool.getResource()) {
            @SuppressWarnings("unchecked")
            List<Object> result = (List<Object>) jedis.eval(
                    script,
                    List.of(RedisKeys.MESSAGES.getKey(channelID)),
                    List.of(String.valueOf(id))
            );

            if (result == null || result.isEmpty()) return null;

            @SuppressWarnings("unchecked")
            List<String> fields = (List<String>) result.get(1);
            Map<String, String> fieldMap = new HashMap<>();
            for (int i = 0; i < fields.size(); i += 2) {
                fieldMap.put(fields.get(i), fields.get(i + 1));
            }

            long userID = Long.parseLong(fieldMap.get("userID"));
            String content = fieldMap.get("content");
            Instant date = Instant.parse(fieldMap.get("date"));

            return new Message(id, userID, content, Date.from(date));
        }
    }

}