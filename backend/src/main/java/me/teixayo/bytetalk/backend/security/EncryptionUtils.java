package me.teixayo.bytetalk.backend.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtils {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
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
}
