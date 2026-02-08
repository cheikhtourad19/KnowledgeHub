package tn.esprim.knowledgehub.user.service;


import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationResponse;
import com.knowledgehub.notification.client.dto.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationService {
    @Autowired
    private NotificationClient notificationClient;

    public NotificationResponse sendWelcomeEmail(String userId, String email, String username) {
        NotificationRequest request = NotificationRequest.builder()
                .content("welcome")
                .recipientEmail(email)
                .type(NotificationType.EMAIL)
                .userId(userId)
                .build();
        return notificationClient.sendNotification(request);
    }
}
