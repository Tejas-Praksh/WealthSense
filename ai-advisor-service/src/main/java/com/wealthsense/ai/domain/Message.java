package com.wealthsense.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String role; // USER or ASSISTANT
    private String content;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private Integer tokens;
}
