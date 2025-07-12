package me.teixayo.bytetalk;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import me.teixayo.bytetalk.backend.Server;
import me.teixayo.bytetalk.backend.security.Crypto;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

@Log4j2
public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair keyPair = kpg.genKeyPair();
        String encrypt = Crypto.encrypt("Hello ljkjhkjkjh kjh kh kjhkjhk kjh kjhjkh kjh khkjh khjkkh world!", keyPair.getPublic());
        System.out.println(encrypt);
        System.out.println(Crypto.decrypt(encrypt, keyPair.getPrivate()));


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
