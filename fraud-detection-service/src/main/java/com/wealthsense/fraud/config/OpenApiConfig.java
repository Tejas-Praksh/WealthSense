package com.wealthsense.fraud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(getServiceName())
                        .description(getDescription())
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("WealthSense Team")
                                .email("api@wealthsense.app"))
                        .license(new License().name("MIT")))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }

    private String getServiceName() {
        return "WealthSense Fraud Detection Service API";
    }

    private String getDescription() {
        return "Fraud detection and risk scoring (Kafka-driven; HTTP for health and docs).";
    }
}
