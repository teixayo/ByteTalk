package me.teixayo.bytetalk.backend.chat;

import lombok.Getter;

import java.util.Date;

@Getter
public class Message {

    private long id;
    private long userID;
    private String content;
    private Date date;

    public Message(long id,long userID,  String content, Date date) {
        this.id = id;
        this.userID = userID;
        this.content = content;
        this.date = date;
    }
}
