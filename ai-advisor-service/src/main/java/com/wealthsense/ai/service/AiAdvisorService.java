package com.wealthsense.ai.service;

import com.wealthsense.ai.domain.Conversation;
import com.wealthsense.ai.domain.Message;
import com.wealthsense.ai.dto.ChatRequest;
import com.wealthsense.ai.dto.ChatResponse;
import com.wealthsense.common.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAdvisorService {

    private final ChatClient chatClient;
    private final ConversationService conversationService;
    private final RagService ragService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int DAILY_LIMIT = 10;
    private static final String RATE_LIMIT_PREFIX = "ai:ratelimit:";
    private static final String CACHE_PREFIX = "ai:cache:";

    public ChatResponse chat(UUID userId, ChatRequest request) {
        checkRateLimit(userId);

        UUID conversationId = request.getConversationId();
        String messageHash = String.valueOf(request.getMessage().hashCode());
        String cacheKey = CACHE_PREFIX + userId + ":" + messageHash;

        // Check Redis Cache
        String cachedResponse = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            log.info("Cache hit for user {}", userId);
            return ChatResponse.builder()
                    .response(cachedResponse)
                    .fromCache(true)
                    .conversationId(conversationId)
                    .build();
        }

        // Setup context and call Claude API
        conversationService.saveMessage(conversationId, userId, "USER", request.getMessage(), request.getMessage().length());

        String userContext = ragService.getUserContext(userId);
        String promptString = ragService.buildPromptWithContext(request.getMessage(), userContext);
        
        List<Message> history = conversationService.getHistory(conversationId, userId);
        StringBuilder historyContext = new StringBuilder();
        history.stream().skip(Math.max(0, history.size() - 5)).forEach(m -> {
            historyContext.append(m.getRole()).append(": ").append(m.getContent()).append("\n");
        });

        String finalPrompt = "History:\n" + historyContext + "\n" + promptString;

        log.info("Calling Claude API via Spring AI ChatClient");
        String aiResponseText = chatClient.prompt(new Prompt(finalPrompt)).call().content();
        
        if(aiResponseText == null || aiResponseText.isEmpty()) {
            aiResponseText = "I'm sorry, I couldn't process your request at the moment.";
        }
        
        Conversation savedConv = conversationService.saveMessage(conversationId, userId, "ASSISTANT", aiResponseText, aiResponseText.length());

        // Cache the response
        redisTemplate.opsForValue().set(cacheKey, aiResponseText, Duration.ofHours(1));

        return ChatResponse.builder()
                .response(aiResponseText)
                .fromCache(false)
                .conversationId(savedConv.getConversationId())
                .tokensUsed(finalPrompt.length() + aiResponseText.length()) // rough estimation
                .build();
    }

    private void checkRateLimit(UUID userId) {
        String key = RATE_LIMIT_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }

        if (count != null && count > DAILY_LIMIT) {
            throw new RateLimitExceededException("Daily limit reached. Upgrade to Pro for unlimited queries.");
        }
    }
}
