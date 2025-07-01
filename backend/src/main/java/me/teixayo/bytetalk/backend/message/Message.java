package me.teixayo.bytetalk.backend.message;

import lombok.Getter;

import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id && userID == message.userID && Objects.equals(content, message.content) && Objects.equals(date, message.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userID, content, date);
    }
}
