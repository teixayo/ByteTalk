package me.teixayo.bytetalk.backend.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;
import org.bson.UuidRepresentation;

public class MongoDBConnection {

//    private static final String CONNECTION_STRING = String.format(
//            "mongodb://%s:%s@%s/?retryWrites=true&w=majority",
//            System.getenv("MONGO_INITDB_ROOT_USERNAME"),
//            System.getenv("MONGO_INITDB_ROOT_PASSWORD"),
//            System.getenv("MongoDB")
//    );
    private static final String CONNECTION_STRING = String.format(
            "mongodb://%s:%s@%s/?retryWrites=true&w=majority",
            "root",
            "",
            "localhost:27017"
    );

    @Getter
    private static MongoDBConnection instance;

    private static MongoClient mongoClient;

    private final MongoDatabase userDatabase;
    @Getter
    private final MongoCollection<Document> userCollection;

    public MongoDBConnection()
    {

        instance=this;
        MongoClientSettings settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                .build();

        mongoClient = MongoClients.create(settings);
        this.userDatabase = mongoClient.getDatabase("User");
        this.userCollection = userDatabase.getCollection("User");
    }

    public static void start()
    {
        if(instance == null)
        {
            new MongoDBConnection();
        }
    }

    public static void stopConnection()
    {
        if(instance != null && mongoClient != null)
        {
            mongoClient.close();
        }
    }
}
