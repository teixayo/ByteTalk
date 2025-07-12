package me.teixayo.bytetalk.backend.service.message;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import lombok.SneakyThrows;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MongoMessageService implements MessageService {

    private final MongoCollection<Document> messages;

    private final ConcurrentHashMap<CompletableFuture<List<Message>>,List<Long>> loadingIdsPools;

    public MongoMessageService() {
        this.messages = MongoDBConnection
                .getInstance()
                .getMessageCollection();
        this.loadingIdsPools = new ConcurrentHashMap<>();
    }

    @SneakyThrows
    @Override
    public List<Message> loadMessagesBeforeDate(Date date, int batchSize) {
        return loadMessagesBeforeDateAsync(date,batchSize).get();
    }

    public CompletableFuture<List<Message>> loadMessagesBeforeDateAsync(Date date, int batchSize) {
        Bson filter = Filters.lt("date", date);
        Bson sort = Sorts.orderBy(Sorts.descending("date"), Sorts.descending("_id"));

        FindIterable<Document> docs = messages
                .find(filter)
                .projection(Projections.include("_id"))
                .sort(sort)
                .limit(batchSize + 1);

        CompletableFuture<List<Message>> future = new CompletableFuture<>();

        List<Long> ids = new ArrayList<>();
        for (Document doc : docs) {
            ids.add(doc.getLong("_id"));
        }
        synchronized (loadingIdsPools) {
            loadingIdsPools.put(future, ids);
        }
        return future;
    }
    public void finalizeAllMessages() {
        Map<CompletableFuture<List<Message>>, List<Long>> snapshot;
        synchronized (loadingIdsPools) {
            if (loadingIdsPools.isEmpty()) return;
            snapshot = new LinkedHashMap<>(loadingIdsPools);
            loadingIdsPools.clear();
        }

        List<Long> ids = new ArrayList<>();
        for (List<Long> snapshotIds : snapshot.values()) {
            ids.addAll(snapshotIds);
        }

        Map<Long, Message> messageMap = new LinkedHashMap<>();
        messages.find(Filters.in("_id", ids))
                .sort(Sorts.orderBy(
                        Sorts.descending("date"),
                        Sorts.descending("_id")
                ))
                .map(this::fromDocument)
                .forEach((Message message) -> {
                    messageMap.put(message.getId(), message);
                });


        for (Map.Entry<CompletableFuture<List<Message>>, List<Long>> entry : snapshot.entrySet()) {
            List<Message> futureMessagesByOrder = new ArrayList<>();
            for (long futureIds : entry.getValue()) {
                futureMessagesByOrder.add(messageMap.get(futureIds));
            }
            entry.getKey().complete(futureMessagesByOrder);
        }
    }
    @Override
    public void saveMessage(Message message) {
        Document document = toDocument(message);
        messages.insertOne(document);
    }


    @Override
    public Message getMessage(long message_id) {
        Document document = messages
                .find(Filters.eq("_id", message_id))
                .first();
        return document == null ? null : fromDocument(document);
    }

    private Document toDocument(Message msg) {
        return new Document("_id", msg.getId())
                .append("userID", msg.getUserID())
                .append("content", msg.getContent())
                .append("date", msg.getDate());
    }

    private Message fromDocument(Document doc) {
        long id = doc.getLong("_id");
        long userID = doc.getLong("userID");
        String content = doc.getString("content");
        Date date = doc.getDate("date");
        return new Message(id, userID, content, date);
    }
}
