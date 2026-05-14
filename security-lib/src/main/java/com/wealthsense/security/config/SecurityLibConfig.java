package com.wealthsense.security.config;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.wealthsense.security.webhook.WebhookSignatureService;
import java.security.Security;

import java.util.Base64;

@Configuration
public class SecurityLibConfig {

    private final Environment environment;

    public SecurityLibConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateEncryptionKey() {
        // Spec requires AES-GCM support via Bouncy Castle.
        Security.addProvider(new BouncyCastleProvider());

        String key = environment.getProperty("encryption.key");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Invalid encryption key. Must be Base64 encoded 32 bytes");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(key);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid encryption key. Must be Base64 encoded 32 bytes");
        }
        if (decoded.length != 32) {
            throw new IllegalStateException("Invalid encryption key. Must be Base64 encoded 32 bytes");
        }
    }

    @Bean
    public WebhookSignatureService webhookSignatureService() {
        String secret = environment.getProperty("webhook.secret");
        return new WebhookSignatureService(secret);
    }
}

