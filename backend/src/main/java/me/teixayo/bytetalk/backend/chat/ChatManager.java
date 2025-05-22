package me.teixayo.bytetalk.backend.chat;

import me.teixayo.bytetalk.backend.user.User;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class ChatManager {

    private static ChatManager instance;

    public static ChatManager getInstance() {
        if(instance==null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void sendMessage(User user, String message) {
        Chat chat = new Chat(user,message, Date.from(Instant.now()));
        saveMessage(chat);
        publishMessage(chat);
    }

    public List<Chat> loadChats(int startIndex, int endIndex) {
        return List.of();
    }



    private void saveMessage(Chat chat) {
        //save it on mongodb
    }

    private void publishMessage(Chat chat) {
        //Send to current node
        //Send to other nodes using redis
    }
}
