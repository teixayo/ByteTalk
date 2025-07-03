package me.teixayo.bytetalk.backend.service.user;

import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import me.teixayo.bytetalk.backend.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MemoryUserService implements UserService {

    private static Map<Long, User> users;

    public MemoryUserService() {
        users = new HashMap<>();
    }

    @Override
    public long saveUser(String username, String password) {
        long userId = RandomGenerator.generateId();
        User user = new User(userId, username, password, null);
        users.put(userId, user);
        return userId;
    }

    @Override
    public boolean isUserExists(String username) {
        return getUserByUserName(username) != null;
    }

    @Override
    public boolean isUserExists(long userId) {
        return users.containsKey(userId);
    }

    @Override
    public String getPasswordByUser(String username) {
        User user = getUserByUserName(username);
        if (user == null) return null;
        return user.getPassword();
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
    public User getUserById(long userId) {
        return users.get(userId);
    }

    @Override
    public HashMap<Long, String> getUsernameByIds(Collection<Long> usersId) {
        HashMap<Long, String> usernames = new HashMap<>(users.size());
        for (long userId : usersId) {
            usernames.put(userId, getUserById(userId).getName());
        }

        return usernames;
    }
}
