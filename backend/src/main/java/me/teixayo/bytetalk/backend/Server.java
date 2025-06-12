package me.teixayo.bytetalk.backend;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.service.message.MessageService;
import me.teixayo.bytetalk.backend.service.search.SearchService;
import me.teixayo.bytetalk.backend.service.user.UserService;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.networking.NettyHandler;
import me.teixayo.bytetalk.backend.user.User;
import me.teixayo.bytetalk.backend.user.UserManager;
import me.teixayo.jegl.loop.LoopApp;
import me.teixayo.jegl.loop.LoopBuilder;
import me.teixayo.jegl.loop.loops.Loop;
import me.teixayo.jegl.loop.loops.LoopType;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Getter
public final class Server implements LoopApp {

    @Getter
    private static Server instance;
    private UserService userService;
    private MessageService messageService;
    private SearchService searchService;
    private NettyHandler nettyHandler;
    private Config config;
    private final Loop loop;

    public Server(){
        instance=this;

        loop = LoopBuilder.builder()
                .loopType(LoopType.LOCK)
                .updatePerSecond(10)
                .loopApp(this)
                .build();
    }

    public void start() {

        log.info("Starting...");

        Yaml yaml = new Yaml();
        InputStream inputStream = Server.class.getClassLoader().getResourceAsStream("config.yml");
        config = new Config(yaml.load(inputStream));

        nettyHandler = new NettyHandler();

        if(config.isMongoToggle()) {
            MongoDBConnection.start(config.getMongoConnectionUrl());
        }
        if(config.isRedisToggle()) {
            RedisDBConnection.start(config.getRedisAddress(),config.getRedisPort(),config.getRedisPassword(),config.isRedisSSL());
        }

        userService = UserService.findBestService();
        log.info("Using {} as UserService", userService.getClass().getSimpleName());

        messageService = MessageService.findBestService();
        log.info("Using {} as MessageService", userService.getClass().getSimpleName());

        searchService = SearchService.findBestService();
        log.info("Using {} as SearchService", userService.getClass().getSimpleName());

    }

    @Override
    public void update() {
        Iterator<Map.Entry<String, User>> iterator = UserManager.getInstance().getUsers().entrySet().iterator();
        while (iterator.hasNext()) {
            User user = iterator.next().getValue();
            user.getUserConnection().checkTimeOut();
            if (!user.getUserConnection().isOnline()) {
                iterator.remove();
                continue;
            }
            user.getUserConnection().processPackets();
        }
    }

    public void close() {
        loop.cancel();
        MongoDBConnection.stop();
        RedisDBConnection.stop();
    }

    public boolean isRunning() {
        return loop.isRunning();
    }

}
