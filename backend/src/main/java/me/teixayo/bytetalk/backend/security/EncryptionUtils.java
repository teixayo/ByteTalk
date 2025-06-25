package me.teixayo.bytetalk.backend.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtils {

    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(byte[] data) {
        return messageDigest.digest(data);
    }
    public static String encrypt(String data) {
        byte[] encryptedData = encrypt(data.getBytes());
        StringBuilder hexDataBuilder = new StringBuilder();
        for (byte b : encryptedData) {
            hexDataBuilder.append(Integer.toHexString(b & 0xff));
        }
        return hexDataBuilder.toString();
    }
}
