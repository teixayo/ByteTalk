package me.teixayo.bytetalk.backend.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.UuidRepresentation;

@Log4j2
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

    private MongoDatabase userDatabase=null;
    @Getter
    private MongoCollection<Document> userCollection=null;
    private MongoDatabase messageDatabase=null;
    @Getter
    private MongoCollection<Document> messageCollection=null;
    @Getter
    private static boolean isConnected = false;

    public MongoDBConnection() {

        instance = this;
        MongoClientSettings settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                .build();
        try {
            mongoClient = MongoClients.create(settings);

            this.messageDatabase = mongoClient.getDatabase("Message");
            this.messageCollection = userDatabase.getCollection("Message");

            this.userDatabase = mongoClient.getDatabase("User");
            this.userCollection = userDatabase.getCollection("User");
            userDatabase.runCommand(new Document("ping", 1));
            isConnected=true;
        } catch (Exception exception) {
            log.error("Failed to connect to MongoDB: ", exception);
        }
    }

    public static void start()
    {
        if(instance == null)
        {
            new MongoDBConnection();
        }
    }

    public static void stop()
    {
        if(instance != null && mongoClient != null)
        {
            mongoClient.close();
            isConnected=false;
        }
    }
}
