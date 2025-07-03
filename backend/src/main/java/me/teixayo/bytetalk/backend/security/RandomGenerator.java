package me.teixayo.bytetalk.backend.security;


import java.security.SecureRandom;
import java.util.Base64;

public class RandomGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public static byte[] generateSecureBytes(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    public static long generateId() {
        return secureRandom.nextLong();
    }


}
