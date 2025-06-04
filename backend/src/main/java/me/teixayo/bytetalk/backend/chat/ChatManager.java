package me.teixayo.bytetalk.backend.chat;

import lombok.Getter;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;

import java.time.Instant;
import java.util.Date;
import java.util.List;

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

    public List<Message> getLatestMessages(){
//        if(RedisDBConnection.getJedisPool()!=null) {
//            return RedisDBConnection.getInstance().toString().;
//        }
        return Server.getInstance().getMessageService().loadMessagesBeforeDate(Date.from(Instant.now()),10);
    }

    public void sendMessage(Message message) {
        if(RedisDBConnection.getJedisPool()==null) return;
//        RedisKe
    }

}
