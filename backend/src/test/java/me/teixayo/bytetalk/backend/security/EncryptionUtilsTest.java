package me.teixayo.bytetalk.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptionUtilsTest {

    @Test
    void testEncryptMethod() {
        assertEquals(Crypto.encryptSHA256("Test"),"532eaabd9574880dbf76b9b8cc00832c20a6ec113d682299550d7a6e0f345e25");

    }

    @Test
    void testIsValidNameMethod() {

        assertTrue(EncryptionUtils.isValidName("A".repeat(19)));
        assertTrue(EncryptionUtils.isValidName("Ali-AA"));
        assertTrue(EncryptionUtils.isValidName("Ali_AA"));

        assertFalse(EncryptionUtils.isValidName("AA"));
        assertFalse(EncryptionUtils.isValidName("AAA  B23"));
        assertFalse(EncryptionUtils.isValidName("3".repeat(20)));
        assertFalse(EncryptionUtils.isValidName("%*&@("));
        assertFalse(EncryptionUtils.isValidName("سلام"));

    }

    @Test
    void testIsValidPasswordMethod() {

        assertTrue(EncryptionUtils.isValidPassword("A".repeat(8)));
        assertTrue(EncryptionUtils.isValidPassword("A".repeat(30)));

        assertFalse(EncryptionUtils.isValidPassword("A".repeat(7)));
        assertFalse(EncryptionUtils.isValidPassword("A".repeat(31)));;


    }
}