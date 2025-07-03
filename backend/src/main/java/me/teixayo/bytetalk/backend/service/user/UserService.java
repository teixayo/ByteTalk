package me.teixayo.bytetalk.backend.service.user;

import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.user.User;

import java.util.Collection;
import java.util.HashMap;

public interface UserService {

    static UserService findBestService() {
        if (MongoDBConnection.isConnected()) return new MongoUserService();
        return new MemoryUserService();
    }

    long saveUser(String username, String password);

    boolean isUserExists(String username);

    boolean isUserExists(long userId);

    String getPasswordByUser(String username);

    User getUserByUserName(String username);

    User getUserById(long userId);

    HashMap<Long, String> getUsernameByIds(Collection<Long> usersId);

}
