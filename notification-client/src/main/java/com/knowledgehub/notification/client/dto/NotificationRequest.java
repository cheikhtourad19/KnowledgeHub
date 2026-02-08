package com.knowledgehub.notification.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String userId;
    private String content;
    private NotificationType type;
    private String subject;
    private String recipientEmail;
    private String deviceToken;
}
