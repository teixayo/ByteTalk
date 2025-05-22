package me.teixayo.bytetalk.backend;

import me.teixayo.bytetalk.backend.networking.NettyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LoggerFactory.getLogger("UncaughtException")
                    .error("Uncaught exception in thread " + thread.getName(), throwable);
        });
//        RedisDBConnection.start();
//        MongoDBConnection.start();
        new NettyHandler("0.0.0.0",25565,"/chat");



    }
}
