package me.teixayo.bytetalk.backend.service.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import me.teixayo.bytetalk.backend.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.HashMap;

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


    @Override
    public HashMap<Long, String> getUsernameByIds(Collection<Long> userIds) {
        HashMap<Long, String> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        Bson filter = Filters.in("_id", userIds);
        try (MongoCursor<Document> cursor = users.find(filter)
                .projection(include("_id", "username"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Long id = doc.getLong("_id");
                String username = doc.getString("username");
                result.put(id, username);
            }
        }
        return result;
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
