package me.teixayo.bytetalk.backend.security.crypto;

import lombok.Getter;
import me.teixayo.bytetalk.backend.security.Crypto;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;

@Getter
public class Key {

    private final java.security.Key privateKey;
    private final java.security.Key publicKey;


    public Key(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public Key(SecretKey secretKey) {
        this.privateKey = secretKey;
        this.publicKey = secretKey;
    }

    public byte[] encrypt(byte[] data, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Crypto.getCipher(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }
    public ByteBuffer encrypt(ByteBuffer data, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        Cipher cipher = Crypto.getCipher(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        data.rewind();
        int outputSize = cipher.getOutputSize(data.remaining());
        ByteBuffer outputBuffer = ByteBuffer.allocate(outputSize);
        cipher.doFinal(data, outputBuffer);
        return outputBuffer;
    }

    public byte[] decrypt(byte[] encryptedData, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Crypto.getCipher(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }
    public ByteBuffer decrypt(ByteBuffer encryptedData, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        Cipher cipher = Crypto.getCipher(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        encryptedData.rewind();
        int outputSize = cipher.getOutputSize(encryptedData.remaining());
        ByteBuffer outputBuffer = ByteBuffer.allocate(outputSize);
        cipher.doFinal(encryptedData, outputBuffer);
        outputBuffer.flip();
        return outputBuffer;
    }
    public static Key generateAsymmetricKey(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            keyPairGen = KeyPairGenerator.getInstance(algorithm,"BC");
        }
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return new Key(keyPair.getPrivate(),keyPair.getPublic());
    }

    public static Key generateSymmetricKey(String algorithm, int bytesSize) throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(algorithm);
        kg.init(bytesSize);
        return new Key(kg.generateKey());
    }

    public static Key generateSymmetricKey(String algorithm, byte[] keyBytes) {
        return new Key(new SecretKeySpec(keyBytes, algorithm));
    }

}
