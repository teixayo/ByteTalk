package me.teixayo.bytetalk.backend.service.user;

import com.mongodb.client.MongoCollection;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import me.teixayo.bytetalk.backend.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;

public class MongoUserService implements UserService {


    private final MongoCollection<Document> users;

    public MongoUserService() {
        users = MongoDBConnection.getInstance().getUserCollection();
    }

    @Override
    public long saveUser(String username, String password) {
        long userId = RandomGenerator.generateId();
        Document doc = new Document("_id", userId)
                .append("username", username)
                .append("password", password);

        users.insertOne(doc);
        return userId;
    }

    @Override
    public boolean isUserExists(String username) {
        return exists(eq("username", username));
    }

    @Override
    public boolean isUserExists(long userId) {
        return exists(eq("_id", userId));
    }

    @Override
    public String getPasswordByUser(String username) {
        Document document = users
                .find(eq("username", username))
                .projection(include("password"))
                .first();
        return document != null ? document.getString("password") : null;
    }

    @Override
    public User getUserByUserName(String username) {
        Document doc = users.find(eq("username", username)).first();
        return doc == null ? null : toUser(doc);
    }

    @Override
    public User getUserById(long userId) {
        Document doc = users.find(eq("_id", userId)).first();
        return doc != null ? toUser(doc) : null;
    }

    private User toUser(Document document) {
        return new User(
                document.getLong("_id"),
                document.getString("username"),
                document.getString("password"),
                null
        );
    }

    private boolean exists(Bson filter) {
        return users
                .find(filter)
                .projection(include("_id"))
                .limit(1)
                .first() != null;
    }
}
