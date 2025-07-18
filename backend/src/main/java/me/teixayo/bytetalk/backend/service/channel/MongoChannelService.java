package me.teixayo.bytetalk.backend.service.channel;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.utils.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoChannelService implements ChannelService {
    private final MongoCollection<Document> channels;
    private final MongoCollection<Document> messages;

    public MongoChannelService() {
        channels = MongoDBConnection
                .getInstance()
                .getChannelCollection();
        messages = MongoDBConnection
                .getInstance()
                .getChannelMessagesCollection();
        messages.createIndex(
                Indexes.compoundIndex(Indexes.ascending("channelId"), Indexes.ascending("date")),
                new IndexOptions().background(true)
        );
        channels.createIndex(
                Indexes.ascending("members"),
                new IndexOptions().background(true)
        );

        if(getChannel(1)==null) {
            createChannel(new Channel(1,"global",Date.from(Instant.now()),List.of(),true));
        }
    }

    @Override
    public void createChannel(Channel channel) {
        channels.insertOne(toDocument(channel));
    }

    @Override
    public Channel getChannel(long channelId) {
        Document document = channels.find(Filters.eq("_id", channelId)).first();
        if (document == null) return null;
        return fromDocument(document);
    }

    @Override
    public Channel getChannelByName(String name) {
        Document document = channels.find(Filters.eq("name", name)).first();
        if (document == null) return null;
        return fromDocument(document);
    }

    @Override
    public void saveMessage(long channelId, long messageId, Date date) {
        Document message = new Document()
                .append("channelId", channelId)
                .append("messageId", messageId)
                .append("date", date);
        messages.insertOne(message);
    }

    @Override
    public List<Long> loadMessagesBeforeDate(long channelId, Date date, int batchSize) {
        Bson filter = Filters.and(
                Filters.eq("channelId", channelId),
                Filters.lt("date", date)
        );
        List<Long> ids = new ArrayList<>();
        messages.find(filter)
                .sort(Sorts.ascending("date"))
                .limit(batchSize)
                .forEach(doc -> ids.add(doc.getLong("messageId")));
        return ids;
    }

    public List<Channel> getUserPrivateChannels(long userId) {
        Bson filter = Filters.in("members", userId);

        List<Pair<Channel,Date>> result = new ArrayList<>();
        channels.find(filter).forEach(doc -> {
            List<Long> members = doc.getList("members", Long.class);
            if (members != null && members.size() == 2) {
                Channel channel = fromDocument(doc);
                Document lastMsg = messages.find(Filters.eq("channelId", channel.getId()))
                        .sort(Sorts.descending("date"))
                        .limit(1)
                        .first();
                if(lastMsg==null) return;
                result.add(Pair.of(channel,lastMsg.getDate("date")));
            }
        });
        result.sort((a, b) -> b.getSecond().compareTo(a.getSecond()));
        List<Channel> sorted = new ArrayList<>();
        for (Pair<Channel,Date> entry : result) {
            sorted.add(entry.getFirst());
        }
        return sorted;
    }

    private Document toDocument(Channel channel) {
        return new Document("_id", channel.getId())
                .append("name", channel.getName())
                .append("createdAt", channel.getCreationDate())
                .append("isGlobal", channel.isGlobal())
                .append("members", channel.getMembers());
    }

    private Channel fromDocument(Document doc) {
        long id = doc.getLong("_id");
        String name = doc.getString("name");
        Date creationDate = doc.getDate("createdAt");
        boolean isGlobal = doc.getBoolean("isGlobal");
        List<Long> members = doc.getList("members", Long.class);
        return new Channel(id, name, creationDate, members, isGlobal);
    }
}