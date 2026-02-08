package tn.esprim.knowledgehub.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.notification.entity.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;

    private String userId;

    private String content;

    private NotificationType type;

    private boolean delivered;

    private boolean read;

    private LocalDateTime createdAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;

    private String subject;
}

