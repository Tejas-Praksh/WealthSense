package com.wealthsense.common.events;

import com.wealthsense.common.enums.NotificationChannel;
import com.wealthsense.common.enums.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private UUID notificationId = UUID.randomUUID();
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String body;
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
    private String templateId;
    @Builder.Default
    private Map<String, String> templateVariables = new HashMap<>();
}
