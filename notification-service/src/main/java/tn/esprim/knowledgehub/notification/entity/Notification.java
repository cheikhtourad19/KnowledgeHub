package tn.esprim.knowledgehub.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    private String userId;
    private String content;
    private NotificationType type;

    private String subject;
    private String recipientEmail;
    private String deviceToken;
    private boolean delivered;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;


    private boolean emailSent;
    private int retryCount;
    private LocalDateTime lastRetryAt;
}