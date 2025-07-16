package me.teixayo.bytetalk.backend.user;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UserManager {
    private static UserManager instance;

    private final ConcurrentHashMap<Long, User> users;

    public UserManager() {
        users = new ConcurrentHashMap<>();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public void removeUser(User user) {
        users.remove(user.getId());
    }
}
