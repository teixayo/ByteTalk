package me.teixayo.bytetalk.backend.security;


import java.security.SecureRandom;
import java.util.Base64;

public class RandomGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return encoder.encodeToString(randomBytes);
    }

    public static long generateId() {
        return secureRandom.nextLong();
    }

}
