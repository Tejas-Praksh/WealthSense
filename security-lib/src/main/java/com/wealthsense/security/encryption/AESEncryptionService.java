package com.wealthsense.security.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM authenticated encryption.
 * Output format: Base64(IV(12 bytes) + ciphertext + tag(16 bytes)).
 */
public class AESEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_SIZE_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom;

    public AESEncryptionService(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be 32 bytes (AES-256).");
        }
        this.keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        this.secureRandom = new SecureRandom();
    }

    public static AESEncryptionService fromBase64Key(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalArgumentException("Encryption key (base64) must be provided.");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new AESEncryptionService(keyBytes);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_SIZE_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_SIZE_BITS, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[IV_SIZE_BYTES + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, IV_SIZE_BYTES);
            System.arraycopy(ciphertext, 0, out, IV_SIZE_BYTES, ciphertext.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed.", e);
        }
    }

    public String decrypt(String ciphertextBase64) {
        if (ciphertextBase64 == null) {
            return null;
        }
        try {
            byte[] allBytes = Base64.getDecoder().decode(ciphertextBase64);
            if (allBytes.length < IV_SIZE_BYTES + 16) {
                throw new IllegalArgumentException("Ciphertext is too short to be valid AES-GCM.");
            }

            byte[] iv = new byte[IV_SIZE_BYTES];
            System.arraycopy(allBytes, 0, iv, 0, IV_SIZE_BYTES);

            int cipherLen = allBytes.length - IV_SIZE_BYTES;
            byte[] cipherBytes = new byte[cipherLen];
            System.arraycopy(allBytes, IV_SIZE_BYTES, cipherBytes, 0, cipherLen);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_SIZE_BITS, iv));
            byte[] plaintext = cipher.doFinal(cipherBytes);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed.", e);
        }
    }

    public String encryptBigDecimal(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return encrypt(amount.toPlainString());
    }

    public boolean isEncrypted(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        // Heuristic: valid base64 and minimum length for our output format.
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return decoded.length >= IV_SIZE_BYTES + 16;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

