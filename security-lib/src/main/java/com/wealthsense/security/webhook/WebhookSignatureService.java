package com.wealthsense.security.webhook;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class WebhookSignatureService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String PREFIX = "sha256=";

    private final byte[] secretBytes;

    public WebhookSignatureService(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("WEBHOOK secret must be provided.");
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateSignature(String payload) {
        if (payload == null) {
            payload = "";
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(rawHmac);
            return PREFIX + hex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }

    public boolean verifySignature(String payload, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        String expected = generateSignature(payload);
        if (!signature.startsWith(PREFIX)) {
            return false;
        }

        byte[] expectedBytes = hexToBytes(expected.substring(PREFIX.length()));
        byte[] actualBytes = hexToBytes(signature.substring(PREFIX.length()));

        // Constant time compare.
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private byte[] hexToBytes(String hex) {
        try {
            if (hex == null || hex.isBlank() || (hex.length() % 2) != 0) {
                return new byte[0];
            }
            return HexFormat.of().parseHex(hex);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}

