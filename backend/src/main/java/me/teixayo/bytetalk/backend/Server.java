package me.teixayo.bytetalk.backend;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.impl.message.MessageService;
import me.teixayo.bytetalk.backend.database.impl.search.SearchService;
import me.teixayo.bytetalk.backend.database.impl.user.UserService;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.networking.NettyHandler;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

@Slf4j
@Getter
public final class Server {

    @Getter
    private static Server instance;
    private UserService userService;
    private MessageService messageService;
    private SearchService searchService;
    private NettyHandler nettyHandler;
    private Config config;
    private final Thread mainThread;

    private boolean running = false;


    public Server(){
        mainThread = new Thread(this::start, "Server");
        instance=this;
    }

    public void start() {
        running = true;
        log.info("Starting...");

        Yaml yaml = new Yaml();
        InputStream inputStream = Server.class.getClassLoader().getResourceAsStream("config.yml");
        config = yaml.loadAs(inputStream, Config.class);

        nettyHandler = new NettyHandler("0.0.0.0",25565,"/chat");

        if(config.getDatabase().getMongodb().isToggle()) {
            MongoDBConnection.start();
        }
        if(config.getDatabase().getRedis().isToggle()) {
            RedisDBConnection.start();
        }

        userService = UserService.findBestService();
        log.info("Using {} as UserService", userService.getClass().getSimpleName());

        messageService = MessageService.findBestService();
        log.info("Using {} as MessageService", userService.getClass().getSimpleName());

        searchService = SearchService.findBestService();
        log.info("Using {} as SearchService", userService.getClass().getSimpleName());

    }

    public void close() {
        MongoDBConnection.stop();
        RedisDBConnection.stop();
        running=false;
    }

}
