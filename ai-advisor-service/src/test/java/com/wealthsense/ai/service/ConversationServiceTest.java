package com.wealthsense.ai.service;

import com.wealthsense.ai.domain.Conversation;
import com.wealthsense.ai.domain.Message;
import com.wealthsense.ai.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationService conversationService;

    private UUID conversationId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void saveMessage_validMessage_savesToMongoDB() {
        Conversation conv = Conversation.builder()
                .conversationId(conversationId)
                .userId(userId)
                .build();

        when(conversationRepository.findByConversationIdAndUserId(conversationId, userId))
                .thenReturn(Optional.of(conv));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conv);

        Conversation result = conversationService.saveMessage(conversationId, userId, "USER", "Hello", 5);

        assertNotNull(result);
        assertEquals(1, result.getMessages().size());
        assertEquals("Hello", result.getMessages().get(0).getContent());
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void getHistory_existingConversation_returnsMessages() {
        Conversation conv = Conversation.builder()
                .conversationId(conversationId)
                .userId(userId)
                .messages(List.of(Message.builder().content("Hi").build()))
                .build();

        when(conversationRepository.findByConversationIdAndUserId(conversationId, userId))
                .thenReturn(Optional.of(conv));

        List<Message> history = conversationService.getHistory(conversationId, userId);

        assertEquals(1, history.size());
        assertEquals("Hi", history.get(0).getContent());
    }

    @Test
    void getHistory_noConversation_returnsEmpty() {
        when(conversationRepository.findByConversationIdAndUserId(conversationId, userId))
                .thenReturn(Optional.empty());

        List<Message> history = conversationService.getHistory(conversationId, userId);

        assertTrue(history.isEmpty());
    }
}
