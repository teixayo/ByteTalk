package me.teixayo.bytetalk.backend.database.impl.user;

import co.elastic.clients.util.Pair;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.user.User;

public interface UserService {

    static UserService findBestService() {
        if(MongoDBConnection.isConnected()) return new MongoUserService();
        return new MemoryUserService();
    }
    Pair<String,Long> saveUser(String username);
    boolean isUserExists(String username);
    boolean isTokenExists(String token);
    boolean isUserExists(long userId);
    String getTokenByUser(String username);

    User getUserByUserName(String username);
    User getUserByToken(String token);
    User getUserById(long userId);

}
