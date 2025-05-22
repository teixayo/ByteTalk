package me.teixayo.bytetalk.backend.chat;

import me.teixayo.bytetalk.backend.user.User;

import java.util.Date;

public class Chat {

    private User user;

    private String message;
    private Date time;

    public Chat(User user, String message,Date time) {
        this.user = user;
        this.message = message;
        this.time = time;
    }

}
