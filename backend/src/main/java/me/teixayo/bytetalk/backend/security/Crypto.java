package me.teixayo.bytetalk.backend.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encryptSHA256(byte[] data) {
        return messageDigest.digest(data);
    }

    public static String encryptSHA256(String data) {
        byte[] encryptedData = encryptSHA256(data.getBytes());
        char[] hexChars = new char[encryptedData.length * 2];

        for (int i = 0; i < encryptedData.length; i++) {
            int v = encryptedData[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
