package com.wealthsense.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET =
            "testSecretKeyThatIsLongEnoughForHS256AlgorithmAtLeast256BitsRequired123";
    private static final long EXPIRATION = 900000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_validUser_returnsToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(
                userId, "user@test.com", "USER", "corr-id-123");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(
                userId, "user@test.com", "USER", "corr-id");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(
                userId, "user@test.com", "USER", "corr-id");

        assertFalse(jwtTokenProvider.validateToken(token + "tampered"));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(SECRET, -1000L);
        UUID userId = UUID.randomUUID();
        String token = shortLivedProvider.generateToken(
                userId, "user@test.com", "USER", "corr-id");

        assertFalse(shortLivedProvider.validateToken(token));
    }

    @Test
    void extractUserId_validToken_returnsCorrectId() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(
                userId, "user@test.com", "USER", "corr-id");

        assertEquals(userId, jwtTokenProvider.extractUserId(token));
    }

    @Test
    void extractEmail_validToken_returnsCorrectEmail() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(
                userId, "admin@test.com", "ADMIN", "corr-id");

        assertEquals("admin@test.com", jwtTokenProvider.extractEmail(token));
    }

    @Test
    void extractRole_validToken_returnsCorrectRole() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(
                userId, "a@b.com", "ANALYST", "corr-id");

        assertEquals("ANALYST", jwtTokenProvider.extractRole(token));
    }
}
