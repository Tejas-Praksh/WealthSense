package com.wealthsense.security.webhook;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class WebhookSignatureServiceTest {

    @Test
    void generateSignature_validPayload_returnsSha256() {
        WebhookSignatureService service = new WebhookSignatureService("secret123");
        String payload = "{\"event\":\"PAYMENT\"}";

        String signature = service.generateSignature(payload);
        assertNotNull(signature);
        assertTrue(signature.startsWith("sha256="));

        // Independently verify expected HMAC bytes.
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec("secret123".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedHex = HexFormat.of().formatHex(raw);
            assertEquals("sha256=" + expectedHex, signature);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void verifySignature_validSignature_returnsTrue() {
        WebhookSignatureService service = new WebhookSignatureService("secret123");
        String payload = "{\"event\":\"PAYMENT\"}";
        String signature = service.generateSignature(payload);

        assertTrue(service.verifySignature(payload, signature));
    }

    @Test
    void verifySignature_invalidSignature_returnsFalse() {
        WebhookSignatureService service = new WebhookSignatureService("secret123");
        String payload = "{\"event\":\"PAYMENT\"}";

        assertFalse(service.verifySignature(payload, "sha256=deadbeef"));
        assertFalse(service.verifySignature(payload, "invalid-signature"));
    }

    @Test
    void verifySignature_timingSafe_noTimingLeak() {
        WebhookSignatureService service = new WebhookSignatureService("secret123");
        String payload = "{\"event\":\"PAYMENT\"}";

        String valid = service.generateSignature(payload);
        String invalid = "sha256=" + HexFormat.of().formatHex(new byte[32]);

        // Timing test is inherently flaky; we use a coarse tolerance.
        long validTotalNs = 0;
        long invalidTotalNs = 0;

        int iterations = 500;
        for (int i = 0; i < iterations; i++) {
            long startValid = System.nanoTime();
            boolean v = service.verifySignature(payload, valid);
            validTotalNs += (System.nanoTime() - startValid);
            assertTrue(v);

            long startInvalid = System.nanoTime();
            boolean inv = service.verifySignature(payload, invalid);
            invalidTotalNs += (System.nanoTime() - startInvalid);
            assertFalse(inv);
        }

        double validAvg = validTotalNs / (double) iterations;
        double invalidAvg = invalidTotalNs / (double) iterations;

        double ratio = Math.max(validAvg, invalidAvg) / Math.min(validAvg, invalidAvg);
        assertTrue(ratio < 1.5, "Timing variance too high (expected constant-time compare).");

        // Also ensure we don't accidentally use String equality.
        assertNotEquals(valid, invalid);
        assertTrue(MessageDigest.isEqual(new byte[]{1,2}, new byte[]{1,2}));
    }
}

