package com.wealthsense.user.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthsense.user.UserServiceApplication;
import com.wealthsense.user.dto.LoginRequest;
import com.wealthsense.user.dto.RegisterRequest;
import com.wealthsense.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = UserServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles({"test", "integration"})
class UserServiceIntegrationTest {

    private static final String ENC_KEY_B64 =
            Base64.getEncoder().encodeToString(new byte[32]);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(6379)));
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("encryption.key", () -> ENC_KEY_B64);
        registry.add("webhook.secret", () -> "test-webhook-secret-value");
        registry.add("spring.task.scheduling.enabled", () -> "false");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void register_validRequest_returnsCreatedAndSuccessBody() throws Exception {
        String email = "integration-" + UUID.randomUUID() + "@test.com";
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .password("Password@123")
                .firstName("Test")
                .lastName("User")
                .phone("9876543210")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.path("success").asBoolean());
    }

    @Test
    void login_afterEmailVerified_returnsAccessToken() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@test.com";
        RegisterRequest reg = RegisterRequest.builder()
                .email(email)
                .password("Password@123")
                .firstName("Test")
                .lastName("User")
                .phone("9876543210")
                .build();

        ResponseEntity<String> regResp = restTemplate.postForEntity(
                "/api/v1/auth/register", reg, String.class);
        assertEquals(HttpStatus.CREATED, regResp.getStatusCode());

        var user = userRepository.findByEmail(email).orElseThrow();
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        LoginRequest login = LoginRequest.builder()
                .email(email)
                .password("Password@123")
                .build();

        ResponseEntity<String> loginResp = restTemplate.postForEntity(
                "/api/v1/auth/login", login, String.class);

        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        JsonNode body = objectMapper.readTree(loginResp.getBody());
        assertTrue(body.path("success").asBoolean());
        assertTrue(body.path("data").path("accessToken").asText().length() > 10);
    }

    @Test
    void login_invalidCredentials_returns401() {
        LoginRequest login = LoginRequest.builder()
                .email("missing-" + UUID.randomUUID() + "@test.com")
                .password("WrongPassword@9")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", login, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
