package me.teixayo.bytetalk.backend.security;


import java.security.SecureRandom;

public class RandomGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static byte[] generateSecureBytes(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    public static long generateId() {
        return secureRandom.nextLong();
    }


}
