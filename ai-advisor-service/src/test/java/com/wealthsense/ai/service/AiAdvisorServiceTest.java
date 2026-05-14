package com.wealthsense.ai.service;

import com.wealthsense.ai.domain.Conversation;
import com.wealthsense.ai.domain.Message;
import com.wealthsense.ai.dto.ChatRequest;
import com.wealthsense.ai.dto.ChatResponse;
import com.wealthsense.common.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAdvisorServiceTest {

    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock
    private ConversationService conversationService;
    @Mock
    private RagService ragService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private AiAdvisorService aiAdvisorService;

    private UUID userId;
    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        chatRequest = ChatRequest.builder()
                .conversationId(UUID.randomUUID())
                .message("How can I save money?")
                .build();
    }

    @Test
    void chat_rateLimitExceeded_throws429() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(11L);

        assertThrows(RateLimitExceededException.class, () -> aiAdvisorService.chat(userId, chatRequest));
    }

    @Test
    void chat_cacheHit_returnsCachedResponse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(5L);
        when(valueOperations.get(anyString())).thenReturn("This is a cached response");

        ChatResponse response = aiAdvisorService.chat(userId, chatRequest);

        assertTrue(response.isFromCache());
        assertEquals("This is a cached response", response.getResponse());
        verify(chatClient, never()).prompt(any(Prompt.class));
    }

    @Test
    void chat_cacheMiss_callsClaudeApiAndReturnsResponse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(5L);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(ragService.getUserContext(userId)).thenReturn("Context");
        when(ragService.buildPromptWithContext(anyString(), anyString())).thenReturn("Prompt Context");
        
        when(conversationService.getHistory(any(), any())).thenReturn(List.of());
        
        Conversation savedConv = Conversation.builder().conversationId(chatRequest.getConversationId()).build();
        when(conversationService.saveMessage(any(), any(), eq("USER"), any(), anyInt())).thenReturn(savedConv);
        when(conversationService.saveMessage(any(), any(), eq("ASSISTANT"), any(), anyInt())).thenReturn(savedConv);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("AI advice on saving money.");

        ChatResponse response = aiAdvisorService.chat(userId, chatRequest);

        assertFalse(response.isFromCache());
        assertEquals("AI advice on saving money.", response.getResponse());
        
        verify(valueOperations).set(anyString(), eq("AI advice on saving money."), any(Duration.class));
    }
}
