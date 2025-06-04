package me.teixayo.bytetalk.backend.user;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class UserManager {
    private static UserManager instance;

    private HashMap<String,User> users;

    public UserManager() {
        users = new HashMap<>();
    }

    public static UserManager getInstance() {
        if(instance==null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void addUser(User user) {
        users.put(user.getName(),user);
    }
}
