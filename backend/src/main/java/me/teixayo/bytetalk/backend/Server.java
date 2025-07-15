package me.teixayo.bytetalk.backend;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.database.mongo.MongoDBConnection;
import me.teixayo.bytetalk.backend.database.redis.RedisChannel;
import me.teixayo.bytetalk.backend.database.redis.RedisDBConnection;
import me.teixayo.bytetalk.backend.message.Message;
import me.teixayo.bytetalk.backend.networking.NettyHandler;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacket;
import me.teixayo.bytetalk.backend.protocol.server.ServerPacketType;
import me.teixayo.bytetalk.backend.security.EncryptionUtils;
import me.teixayo.bytetalk.backend.service.cache.CacheService;
import me.teixayo.bytetalk.backend.service.cache.RedisCacheService;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

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

    @SneakyThrows
    public void start() {

        long start = System.currentTimeMillis();
        log.info("Starting...");

        Yaml yaml = new Yaml();
        InputStream inputStream = Server.class.getClassLoader().getResourceAsStream("config.yml");
        File configFile = new File("config.yml");
        if(!configFile.exists()) {
            try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                log.info("Config file successfully created");
            }
        }
        config = new Config(yaml.load(new FileInputStream(configFile)));

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

        if(RedisDBConnection.isConnected()) {
            RedisDBConnection.getInstance().registerConsumer(RedisChannel.SEND_MESSAGE, data -> {
                log.info("Received Redis Message");
                String username = data.split(" ")[0];
                long messageID = Long.parseLong(data.split(" ")[1]);
                RedisCacheService redisCacheService = (RedisCacheService) cacheService;
                Message message = redisCacheService.getMessageById(messageID);

                ServerPacket sendMessagePacket = ServerPacketType.SendMessage.createPacket(
                        "id", message.getId(),
                        "username", username,
                        "content", message.getContent(),
                        "date", message.getDate().toInstant().toEpochMilli()
                );

                for (User user : UserManager.getInstance().getUsers().values()) {
                    if (user.getName().equals(username)) continue;
                    user.sendPacket(sendMessagePacket);
                }
            });
        }

        long end = System.currentTimeMillis();
        log.info("Loaded sever on {} ms", (end - start));

    }
    @Override
    public void update() {
        for (User user : UserManager.getInstance().getUsers().values()) {
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
