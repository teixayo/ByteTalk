package me.teixayo.bytetalk.backend.database.impl.message;

import me.teixayo.bytetalk.backend.chat.Message;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;

import java.util.Date;
import java.util.List;

public interface MessageService {

    static MessageService findBestService() {
        if(MongoDBConnection.isConnected()) return new MongoMessageService();
        return new MemoryMessageService();
    }

    List<Message> loadMessagesBeforeDate(Date date, int batchSize);
    void saveMessage(Message message);
    Message getMessage(long message_id);
}
