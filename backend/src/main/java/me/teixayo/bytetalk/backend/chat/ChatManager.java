package me.teixayo.bytetalk.backend.chat;

import lombok.Getter;
import me.teixayo.bytetalk.backend.user.User;

import java.time.Instant;
import java.util.Date;

@Getter
public class ChatManager {

    private static ChatManager instance;
    public static ChatManager getInstance() {
        if(instance==null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public ChatManager() {
    }

    public void sendMessage(User user, String message) {
        Message chat = new Message(0,user.getId(),message, Date.from(Instant.now()));
    }
}
