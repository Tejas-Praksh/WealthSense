package com.wealthsense.security.encryption;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EncryptionConfig {

    @Bean
    public AESEncryptionService aesEncryptionService(Environment environment) {
        String base64Key = environment.getProperty("encryption.key");
        return AESEncryptionService.fromBase64Key(base64Key);
    }
}

