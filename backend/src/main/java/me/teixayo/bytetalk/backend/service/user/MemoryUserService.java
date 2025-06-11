package me.teixayo.bytetalk.backend.service.user;

import co.elastic.clients.util.Pair;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import me.teixayo.bytetalk.backend.user.User;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MemoryUserService implements UserService {

    private static Map<Long, User> users;

    public MemoryUserService() {
        users = new HashMap<>();
    }
    @Override
    public Pair<String,Long> saveUser(String username) {
        String token = RandomGenerator.generateToken();
        long userId = RandomGenerator.generateId();
        User user = new User(userId,username,token,null);
        users.put(userId,user);
        return new Pair<>(token,userId);
    }

    @Override
    public boolean isUserExists(String username) {
        return getUserByUserName(username)!=null;
    }

    @Override
    public boolean isTokenExists(String token) {
        return getUserByToken(token)!=null;
    }

    @Override
    public boolean isUserExists(long userId) {
        return users.containsKey(userId);
    }

    @Override
    public String getTokenByUser(String username) {
        User user = getUserByUserName(username);
        if (user == null) return null;
        return user.getToken();
    }

    @Override
    public User getUserByUserName(String username) {
        for (User user : users.values()) {
            if (!user.getName().equals(username)) continue;
            return user;
        }
        return null;
    }

    @Override
    public User getUserByToken(String token) {
        for (User user : users.values()) {
            if (!user.getToken().equals(token)) continue;
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(long userId) {
        return users.get(userId);
    }
}
