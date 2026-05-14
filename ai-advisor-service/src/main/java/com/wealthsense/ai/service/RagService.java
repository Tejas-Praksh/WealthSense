package com.wealthsense.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.wealthsense.common.dto.ApiResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    // Ideally, we'd inject a named WebClient, but for now we create a generic one.
    // In a real microservices env, you'd use @LoadBalanced WebClient or Feign.
    private final WebClient webClient = WebClient.builder().baseUrl("http://localhost:8080").build();

    public String getUserContext(UUID userId) {
        try {
            // Dummy implementation of REST call to transaction-service for context
            ApiResponse<List<Map<String, Object>>> response = webClient.get()
                .uri("/api/v1/transactions?userId=" + userId + "&limit=30")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Map<String, Object>>>>() {})
                .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                // In reality, we format the recent transactions into a readable summary string
                return "Recent transactions count: " + response.getData().size();
            }
        } catch (Exception e) {
            log.warn("Could not fetch user context for user {}: {}", userId, e.getMessage());
        }
        return "Not available.";
    }

    public String buildPromptWithContext(String userMessage, String userContext) {
        return String.format(
            "User's Financial Context: %s\n\nUser Question: %s",
            userContext, userMessage
        );
    }
}
