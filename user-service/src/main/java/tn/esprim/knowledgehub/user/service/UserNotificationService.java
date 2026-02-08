package tn.esprim.knowledgehub.user.service;


import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationService {
    @Autowired
    private NotificationClient notificationClient;

    /**
     * Send welcome email asynchronously - doesn't block user creation
     */
    public void sendWelcomeEmail(String userId, String email, String username) {
        NotificationRequest request = NotificationRequest.builder()
                .content("Welcome to KnowledgeHub, " + username + "! We're excited to have you on board.")
                .recipientEmail(email)
                .subject("Welcome to KnowledgeHub")
                .type(NotificationType.EMAIL)
                .userId(userId)
                .build();

        // Use async method - fire and forget, doesn't block
        notificationClient.sendNotificationAsync(request);
    }
}
