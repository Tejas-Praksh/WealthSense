package com.wealthsense.transaction.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.transaction.TransactionServiceApplication;
import com.wealthsense.transaction.domain.Account;
import com.wealthsense.transaction.dto.CreateTransactionRequest;
import com.wealthsense.transaction.repository.AccountRepository;
import com.wealthsense.transaction.repository.OutboxRepository;
import com.wealthsense.transaction.service.OutboxPublisherService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = TransactionServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles({"test", "integration"})
class TransactionServiceIntegrationTest {

    private static final String ENC_KEY_B64 =
            Base64.getEncoder().encodeToString(new byte[32]);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("txn_it")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(6379)));
        registry.add("encryption.key", () -> ENC_KEY_B64);
        registry.add("webhook.secret", () -> "test-webhook-secret");
        registry.add("spring.task.scheduling.enabled", () -> "false");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OutboxPublisherService outboxPublisherService;

    private UUID userId;
    private UUID accountId;
    private KafkaConsumer<String, String> kafkaConsumer;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        Account account = Account.builder()
                .userId(userId)
                .accountNumber("WS" + UUID.randomUUID().toString().substring(0, 8))
                .balance(new BigDecimal("100000"))
                .availableBalance(new BigDecimal("100000"))
                .currency("INR")
                .accountType("SAVINGS")
                .active(true)
                .build();
        accountId = accountRepository.save(account).getId();

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "it-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConsumer = new KafkaConsumer<>(consumerProps);
        kafkaConsumer.subscribe(Collections.singletonList("transaction-events"));
        // Drain old messages
        kafkaConsumer.poll(Duration.ofMillis(200));
    }

    @AfterEach
    void tearDown() {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
    }

    @Test
    void createTransaction_thenPublishOutbox_deliversKafkaMessage() throws Exception {
        String idem = "idem-" + UUID.randomUUID();
        CreateTransactionRequest body = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("1000"))
                .type(TransactionType.DEBIT)
                .merchantName("IntegrationMerchant")
                .category("FOOD")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-ID", userId.toString());
        headers.set("X-Idempotency-Key", idem);
        headers.set("X-Correlation-ID", UUID.randomUUID().toString());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/transactions",
                new HttpEntity<>(body, headers),
                String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertTrue(root.path("success").asBoolean());
        UUID transactionId = UUID.fromString(root.path("data").path("id").asText());
        assertNotNull(transactionId);

        assertFalse(outboxRepository.findAll().stream()
                .filter(e -> e.getAggregateId().equals(transactionId))
                .toList()
                .isEmpty());

        outboxPublisherService.publishPendingEvents();

        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(10));
        boolean found = false;
        for (ConsumerRecord<String, String> rec : records) {
            if (rec.value().contains(transactionId.toString())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Kafka should contain payload referencing transaction id");
    }

    @Test
    void createTransaction_sameIdempotencyKey_returnsSameTransactionId() throws Exception {
        String idem = "idem-dup-" + UUID.randomUUID();
        CreateTransactionRequest body = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("2000"))
                .type(TransactionType.DEBIT)
                .merchantName("DupTest")
                .category("FOOD")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-ID", userId.toString());
        headers.set("X-Idempotency-Key", idem);

        HttpEntity<CreateTransactionRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> first = restTemplate.postForEntity(
                "/api/v1/transactions", entity, String.class);
        ResponseEntity<String> second = restTemplate.postForEntity(
                "/api/v1/transactions", entity, String.class);

        assertEquals(HttpStatus.CREATED, first.getStatusCode());
        assertEquals(HttpStatus.CREATED, second.getStatusCode());

        JsonNode id1 = objectMapper.readTree(first.getBody()).path("data").path("id");
        JsonNode id2 = objectMapper.readTree(second.getBody()).path("data").path("id");
        assertEquals(id1.asText(), id2.asText());
    }
}
