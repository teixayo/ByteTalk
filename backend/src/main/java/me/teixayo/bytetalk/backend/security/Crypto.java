package me.teixayo.bytetalk.backend.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

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

    public static String encryptSHA256(String message, PublicKey rsaPublicKey) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey aesKey = keyGen.generateKey();

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedMessage = aesCipher.doFinal(message.getBytes());

        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        ByteBuffer buffer = ByteBuffer.allocate(4 + encryptedAesKey.length + encryptedMessage.length);
        buffer.putInt(encryptedAesKey.length);
        buffer.put(encryptedAesKey);
        buffer.put(encryptedMessage);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    public static String decrypt(String base64Payload, PrivateKey rsaPrivateKey) throws Exception {
        byte[] payload = Base64.getDecoder().decode(base64Payload);
        ByteBuffer buffer = ByteBuffer.wrap(payload);

        int aesKeyLength = buffer.getInt();
        byte[] encryptedAesKey = new byte[aesKeyLength];
        buffer.get(encryptedAesKey);

        byte[] encryptedMessage = new byte[buffer.remaining()];
        buffer.get(encryptedMessage);

        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedMessage = aesCipher.doFinal(encryptedMessage);

        return new String(decryptedMessage);
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
