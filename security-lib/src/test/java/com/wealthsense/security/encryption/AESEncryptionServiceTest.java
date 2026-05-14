package com.wealthsense.security.encryption;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AESEncryptionServiceTest {

    private static AESEncryptionService createService() {
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) (i + 1);
        }
        return new AESEncryptionService(keyBytes);
    }

    @Test
    void encrypt_validText_returnsEncrypted() {
        AESEncryptionService service = createService();
        String encrypted = service.encrypt("hello");
        assertNotNull(encrypted);
        assertNotEquals("hello", encrypted);
        assertTrue(service.isEncrypted(encrypted));
    }

    @Test
    void decrypt_encryptedText_returnsOriginal() {
        AESEncryptionService service = createService();
        String encrypted = service.encrypt("hello");
        String decrypted = service.decrypt(encrypted);
        assertEquals("hello", decrypted);
    }

    @Test
    void encrypt_sameText_differentCiphertext() {
        AESEncryptionService service = createService();
        String encrypted1 = service.encrypt("hello");
        String encrypted2 = service.encrypt("hello");
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void decrypt_tamperedCiphertext_throwsException() {
        AESEncryptionService service = createService();
        String encrypted = service.encrypt("hello");

        byte[] decoded = java.util.Base64.getDecoder().decode(encrypted);
        decoded[decoded.length - 1] = (byte) (decoded[decoded.length - 1] ^ 0x01);
        String tampered = java.util.Base64.getEncoder().encodeToString(decoded);

        assertThrows(RuntimeException.class, () -> service.decrypt(tampered));
    }

    @Test
    void encrypt_decrypt_bigDecimal_correctly() {
        AESEncryptionService service = createService();
        BigDecimal amount = new BigDecimal("12345.67");
        String encrypted = service.encryptBigDecimal(amount);
        String decrypted = service.decrypt(encrypted);
        assertEquals(amount.toPlainString(), decrypted);
    }

    @Test
    void isEncrypted_plainText_returnsFalse() {
        AESEncryptionService service = createService();
        assertFalse(service.isEncrypted("not-base64!!"));
    }

    @Test
    void isEncrypted_encryptedText_returnsTrue() {
        AESEncryptionService service = createService();
        String encrypted = service.encrypt("hello");
        assertTrue(service.isEncrypted(encrypted));
    }
}

