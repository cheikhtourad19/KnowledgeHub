package tn.esprim.knowledgehub.notification.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String userId;

    private String content;

    private NotificationType type;

    private boolean delivered;

    private boolean read; // Only applicable for push notifications

    private LocalDateTime createdAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;

    // Additional metadata
    private String subject; // For email notifications

    private String recipientEmail; // For email notifications

    private String deviceToken; // For push notifications
}