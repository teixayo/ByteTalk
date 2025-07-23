package me.teixayo.bytetalk.backend;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.teixayo.bytetalk.backend.networking.TransportType;
import me.teixayo.bytetalk.backend.security.RandomGenerator;
import me.teixayo.bytetalk.backend.security.RateLimiter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Getter
public class Config {

    private final Map<String, Object> data;

    private final boolean redisToggle;
    private final String redisAddress;
    private final int redisPort;
    private final String redisUser;
    private final String redisPassword;
    private final boolean redisSSL;
    private final String redisPrefix;

    private final boolean mongoToggle;
    private final String mongoConnectionUrl;

    private final boolean elasticToggle;
    private final String elasticAddress;
    private final int elasticPort;
    private final String elasticUser;
    private final String elasticPassword;

    private final String networkingIp;
    private final int networkingPort;
    private final boolean networkingTcpFastOpen;
    private final boolean networkingTcpNoDelay;
    private final TransportType networkingTransportType;
    private final int networkingWriteBufferWaterMarkLow;
    private final int networkingWriteBufferWaterMarkHigh;

    private final int cacheMessageSize;

    private final int maxTimeOut;

    private final boolean sslToggleUsingWSS;
    private final boolean sslToggleUsingKeys;
    private final RateLimiter sendMessageLimiter;
    private final int maxSendMessageSize;
    private final RateLimiter bulkMessageLimiter;
    private final int authenticationDelay;
    private File sslCertifiateFile = null;
    private File sslPrivateKeyFile = null;
    private final byte[] jwtSecret;

    @SneakyThrows
    public Config(Map<String, Object> data) {
        this.data = data;
        redisToggle = (boolean) get("database.redis.toggle");
        redisAddress = (String) get("database.redis.address");
        redisPort = (int) get("database.redis.port");
        redisUser = (String) get("database.redis.user");
        redisPassword = (String) get("database.redis.password");
        redisSSL = (boolean) get("database.redis.ssl");
        redisPrefix = (String) get("database.redis.prefix");

        mongoToggle = (boolean) get("database.mongo.toggle");
        mongoConnectionUrl = (String) get("database.mongo.connectionURL");

        elasticToggle = (boolean) get("database.elastic.toggle");
        elasticAddress = (String) get("database.elastic.address");
        elasticPort = (int) get("database.elastic.port");
        elasticUser = (String) get("database.elastic.user");
        elasticPassword = (String) get("database.elastic.password");

        networkingIp = (String) get("networking.ip");
        networkingPort = (int) get("networking.port");
        networkingTcpFastOpen = (boolean) get("networking.tcp-fast-open");
        networkingTcpNoDelay = (boolean) get("networking.tcp-no-delay");
        String transport = ((String) get("networking.transport")).toUpperCase();

        networkingTransportType = transport.equals("AUTO") ? TransportType.bestTransportType() : TransportType.valueOf(transport);

        maxTimeOut = (int) get("networking.max-time-out");

        networkingWriteBufferWaterMarkLow = (int) get("networking.write-buffer-watermark.low");
        networkingWriteBufferWaterMarkHigh = (int) get("networking.write-buffer-watermark.high");

        cacheMessageSize = (int) get("cache.message-size");


        sslToggleUsingKeys = (boolean) get("ssl.toggleUsingKeys");
        sslToggleUsingWSS = (boolean) get("ssl.toggleUsingWSS") | sslToggleUsingKeys;

        if (sslToggleUsingKeys) {
            sslCertifiateFile = new File((String) get("ssl.certificateFile"));
            sslPrivateKeyFile = new File((String) get("ssl.privateKeyFile"));
            if (!sslCertifiateFile.exists() || !sslPrivateKeyFile.exists()) {
                throw new IllegalArgumentException("ssl keys doesn't exists");
            }
        }
        sendMessageLimiter = new RateLimiter(
                (int) get("rate-limiter.send-message.tokens"),
                (int) get("rate-limiter.send-message.time")
        );
        maxSendMessageSize = (int) get("rate-limiter.send-message.max-size");
        bulkMessageLimiter = new RateLimiter(
                (int) get("rate-limiter.bulk-message.tokens"),
                (int) get("rate-limiter.bulk-message.time")
        );
        authenticationDelay = (int) get("rate-limiter.authentication.delay");


        File jwtSecretFile = new File((String) get("jwt-secret.file"));
        if (!jwtSecretFile.exists()) {
            log.warn("JWT secret not found, generating a new one...");
            jwtSecret = generateJWTSecretFile(jwtSecretFile);
        } else {
            String encryptedSecretKey = Files.readString(Path.of(jwtSecretFile.getPath()));
            jwtSecret = Base64.getDecoder().decode(encryptedSecretKey);
        }


        log.info("Config Loaded");
    }

    @SuppressWarnings("unchecked")
    public Object get(String path) {
        String[] parts = path.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                throw new NullPointerException(path + " doesn't exists");
            }
        }

        return current;
    }


    @SneakyThrows
    private byte[] generateJWTSecretFile(File file) {
        byte[] secretBytes = RandomGenerator.generateSecureBytes(512);
        String base64Key = Base64.getEncoder().encodeToString(secretBytes);
        file.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(base64Key.getBytes());
        }
        log.info("JWT secret key has been saved to: {}", file.getAbsolutePath());
        return secretBytes;
    }


}
