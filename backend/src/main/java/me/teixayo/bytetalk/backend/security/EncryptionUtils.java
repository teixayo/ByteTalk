package me.teixayo.bytetalk.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;

public class EncryptionUtils {

    @Getter
    private static final HashMap<String, String> tokens = new HashMap<>();
    @Getter
    private static final Algorithm algorithm;
    private static final JWTVerifier verifier;

    static {
        algorithm = Algorithm.HMAC256(RandomGenerator.generateSecureBytes(32));
        verifier = JWT.require(algorithm).build();
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-z][A-Za-z0-9__-]{3,19}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.matches("^[\\S]{8,30}$");
    }

    public static String createLoginJWT(String username) {
        String token = JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .sign(algorithm);
        tokens.put(username, token);
        return token;
    }

    public static DecodedJWT getJWT(String username) {
        if (!tokens.containsKey(username)) return null;
        String token = tokens.get(username);
        DecodedJWT jwt = verifier.verify(token);
        if (jwt.getExpiresAt().toInstant().isBefore(Instant.now())) {
            tokens.remove(username);
            return null;
        }
        return jwt;
    }
}
