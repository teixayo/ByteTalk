package me.teixayo.bytetalk.backend.user;

import me.teixayo.bytetalk.backend.security.TokenGenerator;

import java.util.HashMap;

public class UserManager {
    private static UserManager instance;

    private HashMap<String,User> users;

    public static UserManager getInstance() {
        if(instance==null) {
            instance = new UserManager();
        }
        return instance;
    }

    public String createToken(String name) {
        String token = TokenGenerator.generateToken();
        createUserInDB(name, token);
        return token;
    }

    public void addUser(User user) {
        users.put(user.getName(),user);
        createUserInDB(user.getName(),user.getToken());
    }

    public boolean isUserExists(String name) {
//        return MongoDBConnection.getInstance().getUserCollection()
//                .find(Filters.eq("name", name))
//                .iterator()
//                .hasNext();
        return true;
    }

    public void createUserInDB(String name, String token) {
//        Document document = new Document();
//        document.put("name",name);
//        document.put("token",token);
//        MongoDBConnection.getInstance().getUserCollection().insertOne(document);
    }
    public String getToken(String name) {
//        Document doc = MongoDBConnection.getInstance().getUserCollection().find(Filters.eq("name", name)).first();
//        if (doc == null) {
//            return null;
//        }
//
//        return doc.getString("token");
        return "";
    }
}
