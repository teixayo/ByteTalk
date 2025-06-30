package me.teixayo.bytetalk.backend.service.message;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoMessageService implements MessageService {

    private final MongoCollection<Document> messages;

    public MongoMessageService() {
        this.messages = MongoDBConnection
                .getInstance()
                .getMessageCollection();
    }

    @Override
    public List<Message> loadMessagesBeforeDate(Date date, int batchSize) {
        Bson filter = Filters.lt("date", date);
        Bson sort = Sorts.orderBy(Sorts.descending("date"), Sorts.descending("_id"));

        FindIterable<Document> docs = messages
                .find(filter)
                .sort(sort)
                .limit(batchSize);

        List<Message> result = new ArrayList<>(batchSize);
        for (Document doc : docs) {
            result.add(fromDocument(doc));
        }
        return result;
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
        return new Document("id", msg.getId())
                .append("userID", msg.getUserID())
                .append("content", msg.getContent())
                .append("date", msg.getDate());
    }

    private Message fromDocument(Document doc) {
        long id = doc.getLong("id");
        long userID = doc.getLong("userID");
        String content = doc.getString("content");
        Date date = doc.getDate("date");
        return new Message(id, userID, content, date);
    }
}
