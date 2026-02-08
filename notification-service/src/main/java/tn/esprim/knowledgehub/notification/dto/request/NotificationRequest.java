package tn.esprim.knowledgehub.notification.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.notification.entity.NotificationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    // For email notifications
    private String subject;

    @NotBlank(message = "Recipient email is required for email notifications")
    private String recipientEmail;

    // For push notifications
    private String deviceToken;
}

