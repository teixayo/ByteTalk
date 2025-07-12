package me.teixayo.bytetalk.backend.service.channel;

import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
public class Channel {
    private final long id;
    private final String name;
    private final Date creationDate;
    private final List<Long> members;
    private final boolean isGlobal;
    public Channel(long id, String name, Date creationDate, List<Long> members, boolean isGlobal) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.members = members;
        this.isGlobal = isGlobal;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return id == channel.id && isGlobal == channel.isGlobal && Objects.equals(name, channel.name) && Objects.equals(creationDate, channel.creationDate) && Objects.equals(members, channel.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creationDate, members, isGlobal);
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creationDate=" + creationDate +
                ", members=" + members +
                ", isGlobal=" + isGlobal +
                '}';
    }
}
