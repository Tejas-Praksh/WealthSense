package com.wealthsense.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID eventId = UUID.randomUUID();
    private String eventType;
    private String correlationId;
    private UUID userId;
    private Instant timestamp = Instant.now();
    private int version = 1;
    private String source;
}
