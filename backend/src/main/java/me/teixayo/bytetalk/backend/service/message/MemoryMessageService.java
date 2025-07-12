package me.teixayo.bytetalk.backend.service.message;

import me.teixayo.bytetalk.backend.security.RandomGenerator;

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
    public List<Message> loadMessagesBeforeDate(Date date, int batchSize) {
        Message ceiling = new Message(RandomGenerator.generateId(), RandomGenerator.generateId(), "", date);

        NavigableSet<Message> head = messagesByDate.headSet(ceiling, false);

        List<Message> result = new ArrayList<>(batchSize);
        Iterator<Message> it = head.descendingIterator();
        int count = 0;
        while (it.hasNext() && count < batchSize) {
            result.add(it.next());
            count++;
        }
        return result;
    }

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


}
