package com.wealthsense.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @InjectMocks
    private RagService ragService;

    @Test
    void getUserContext_returnsDefaultWhenWebClientFails() {
        // As our test setup doesn't mock WebClient completely, it should hit the catch block
        String context = ragService.getUserContext(UUID.randomUUID());
        assertEquals("Not available.", context);
    }

    @Test
    void buildPromptWithContext_includesUserData() {
        String message = "What should I do?";
        String context = "Recent transactions count: 5";

        String prompt = ragService.buildPromptWithContext(message, context);

        assertTrue(prompt.contains(message));
        assertTrue(prompt.contains(context));
        assertTrue(prompt.contains("User's Financial Context:"));
    }
}
