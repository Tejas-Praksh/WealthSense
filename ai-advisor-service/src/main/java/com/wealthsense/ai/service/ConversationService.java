package com.wealthsense.ai.service;

import com.wealthsense.ai.domain.Conversation;
import com.wealthsense.ai.domain.Message;
import com.wealthsense.ai.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public Conversation saveMessage(UUID conversationId, UUID userId, String role, String content, Integer tokens) {
        Conversation conversation;
        
        if (conversationId != null) {
            Optional<Conversation> existingOpt = conversationRepository.findByConversationIdAndUserId(conversationId, userId);
            conversation = existingOpt.orElseGet(() -> createNewConversation(conversationId, userId));
        } else {
            conversation = createNewConversation(UUID.randomUUID(), userId);
        }

        Message message = Message.builder()
                .role(role)
                .content(content)
                .tokens(tokens)
                .build();

        conversation.addMessage(message);
        return conversationRepository.save(conversation);
    }

    private Conversation createNewConversation(UUID conversationId, UUID userId) {
        return Conversation.builder()
                .conversationId(conversationId)
                .userId(userId)
                .build();
    }

    public List<Message> getHistory(UUID conversationId, UUID userId) {
        if (conversationId == null) {
            return List.of();
        }
        return conversationRepository.findByConversationIdAndUserId(conversationId, userId)
                .map(Conversation::getMessages)
                .orElse(List.of());
    }

    public List<Conversation> getUserConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }
}
