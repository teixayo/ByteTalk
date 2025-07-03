package me.teixayo.bytetalk.backend.service.message;

import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.message.Message;

import java.util.Date;
import java.util.List;

public interface MessageService {

    static MessageService findBestService() {
        if (MongoDBConnection.isConnected()) return new MongoMessageService();
        return new MemoryMessageService();
    }

    List<Message> loadMessagesBeforeDate(Date date, int batchSize);

    void saveMessage(Message message);

    Message getMessage(long message_id);
}
