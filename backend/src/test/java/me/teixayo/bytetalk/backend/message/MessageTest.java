package me.teixayo.bytetalk.backend.message;

import me.teixayo.bytetalk.backend.service.message.Message;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;

class MessageTest {


    @Test
    public void testConstruction() {
        int id = 10;
        int userId = 40;
        String content = "Test";
        Date from = Date.from(Instant.now());

        Message message = new Message(id, userId, content, from);
        assertEquals(id, message.getId());
        assertEquals(userId, message.getUserID());
        assertEquals(content, message.getContent());
        assertEquals(from, message.getDate());
    }
}