package me.teixayo.bytetalk.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;

public class EncryptionUtils {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    @Getter
    private static HashMap<String,String> tokens = new HashMap<>();
    @Getter
    private static Algorithm algorithm;
    private static JWTVerifier verifier;
    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            algorithm = Algorithm.HMAC256(RandomGenerator.generateSecureBytes(32));
            verifier = JWT.require(algorithm)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(byte[] data) {
        return messageDigest.digest(data);
    }

    public static String encrypt(String data) {
        byte[] encryptedData = encrypt(data.getBytes());
        char[] hexChars = new char[encryptedData.length * 2];

        for (int i = 0; i < encryptedData.length; i++) {
            int v = encryptedData[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-z][A-Za-z0-9__-]{3,19}$");
    }
    public static boolean isValidPassword(String password) {
        return password !=null && password.matches("^[\\S]{8,30}$");
    }

    public static String createLoginJWT(String username) {
        String token = JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .sign(algorithm);
        tokens.put(username,token);
        return token;
    }
    public static DecodedJWT getJWT(String username) {
        if(!tokens.containsKey(username)) return null;
        String token =tokens.get(username);
        DecodedJWT jwt = verifier.verify(token);
        if (jwt.getExpiresAt().toInstant().isBefore(Instant.now())) {
            tokens.remove(username);
            return null;
        }
        return jwt;
    }
}
