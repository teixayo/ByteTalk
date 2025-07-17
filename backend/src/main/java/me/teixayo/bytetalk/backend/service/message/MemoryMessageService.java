package me.teixayo.bytetalk.backend.service.message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class MemoryMessageService implements MessageService {

    private final Map<Long, Message> messagesById = new ConcurrentHashMap<>();

    private final NavigableSet<Message> messagesByDate =
            new ConcurrentSkipListSet<>(Comparator
                    .comparing(Message::getDate)
                    .thenComparing(Message::getId));

    @Override
    public void saveMessage(Message message) {
        Message old = messagesById.put(message.getId(), message);
        if (old != null) {
            messagesByDate.remove(old);
        }
        messagesByDate.add(message);
    }

    @Override
    public Message getMessage(long message_id) {
        return messagesById.get(message_id);
    }

    @Override
    public List<Message> getMessage(List<Long> messages_id) {
        List<Message> result = new ArrayList<>();
        for (Long id : messages_id) {
            result.add(messagesById.get(id));
        }
        return result;
    }


}