package com.wealthsense.ai.dto;

import com.wealthsense.ai.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {

    private UUID conversationId;
    private List<Message> messages;
    private Instant createdAt;
    private Instant updatedAt;
}
