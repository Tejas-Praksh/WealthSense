package com.wealthsense.ai.repository;

import com.wealthsense.ai.domain.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    
    Optional<Conversation> findByConversationIdAndUserId(UUID conversationId, UUID userId);
    
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId);
}
