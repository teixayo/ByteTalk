package me.teixayo.bytetalk.backend.service.message;

import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;

import java.util.List;

public interface MessageService {

    static MessageService findBestService() {
        if (MongoDBConnection.isConnected()) return new MongoMessageService();
        return new MemoryMessageService();
    }


    void saveMessage(Message message);

    Message getMessage(long message_id);
    List<Message> getMessage(List<Long> messages_id);

}
