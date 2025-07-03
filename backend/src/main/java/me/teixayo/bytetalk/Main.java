package me.teixayo.bytetalk;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import me.teixayo.bytetalk.backend.Server;
import org.slf4j.LoggerFactory;

@Log4j2
public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LoggerFactory.getLogger("UncaughtException")
                    .error("Uncaught exception in thread {}", thread.getName(), throwable);
        });
        Server server = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server.isRunning()) {
                server.close();
            }
        }));
        Thread serverThread = new Thread(() -> {
            server.getLoop().start();
        });
        serverThread.setName("Server");
        serverThread.start();
    }
}
