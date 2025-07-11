package me.teixayo.bytetalk.backend;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.networking.NettyHandler;
import me.teixayo.bytetalk.backend.security.EncryptionUtils;
import me.teixayo.bytetalk.backend.service.cache.CacheService;
import me.teixayo.bytetalk.backend.service.message.MessageService;
import me.teixayo.bytetalk.backend.service.message.MongoMessageService;
import me.teixayo.bytetalk.backend.service.search.SearchService;
import me.teixayo.bytetalk.backend.service.user.UserService;
import me.teixayo.bytetalk.backend.user.User;
import me.teixayo.bytetalk.backend.user.UserManager;
import me.teixayo.jegl.loop.LoopApp;
import me.teixayo.jegl.loop.LoopBuilder;
import me.teixayo.jegl.loop.loops.Loop;
import me.teixayo.jegl.loop.loops.LoopType;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Getter
public final class Server implements LoopApp {

    @Getter
    private static Server instance;
    private final Loop loop;
    private UserService userService;
    private MessageService messageService;
    private SearchService searchService;
    private CacheService cacheService;
    private NettyHandler nettyHandler;
    private Config config;

    public Server() {
        instance = this;

        loop = LoopBuilder.builder()
                .loopType(LoopType.LOCK)
                .updatePerSecond(10)
                .loopApp(this)
                .build();
    }

    public void start() {

        long start = System.currentTimeMillis();
        log.info("Starting...");

        Yaml yaml = new Yaml();
        InputStream inputStream = Server.class.getClassLoader().getResourceAsStream("config.yml");
        config = new Config(yaml.load(inputStream));

        nettyHandler = new NettyHandler();

        if (config.isMongoToggle()) {
            MongoDBConnection.start(config.getMongoConnectionUrl());
        }
        if (config.isRedisToggle()) {
            RedisDBConnection.start(config.getRedisAddress(), config.getRedisPort(), config.getRedisPassword(), config.isRedisSSL());
        }

        userService = UserService.findBestService();
        log.info("Using {} as UserService", userService.getClass().getSimpleName());

        messageService = MessageService.findBestService();
        log.info("Using {} as MessageService", messageService.getClass().getSimpleName());

        searchService = SearchService.findBestService();
        log.info("Using {} as SearchService", searchService.getClass().getSimpleName());

        cacheService = CacheService.findBestService();
        log.info("Using {} as CacheService", cacheService.getClass().getSimpleName());

        long end = System.currentTimeMillis();
        log.info("Loaded sever on {} ms", (end - start));

    }

    private boolean a;
    @Override
    public void update() {
        for (User user : UserManager.getInstance().getUsers().values()) {
            if(!a) {
                for(int i = 0 ; i < 1000; i++) {
                    Message message = new Message(i,user.getId(), Integer.toString(i), Date.from(Instant.now()));
                    cacheService.addMessageToCache(message);
                    messageService.saveMessage(message);
                }
                a=true;
            }
            user.getUserConnection().checkTimeOut();
            if (!user.getUserConnection().isOnline()) {
                UserManager.getInstance().removeUser(user);
                continue;
            }
            user.getUserConnection().processPackets();
        }
        if (loop.getUpdates() % 10000 == 0) {
            for (String token : EncryptionUtils.getTokens().keySet()) {
                if (EncryptionUtils.getJWT(token) == null) {
                    log.info("{} token expired", token);
                }

            }
        }
        if(messageService instanceof MongoMessageService mongoMessageService) {
            mongoMessageService.finalizeAllMessages();
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
