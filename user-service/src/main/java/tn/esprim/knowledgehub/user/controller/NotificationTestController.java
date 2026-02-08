package tn.esprim.knowledgehub.user.controller;

import com.knowledgehub.notification.client.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.user.service.UserNotificationService;

/**
 * TEST CONTROLLER - Use this to manually test notifications
 * Remove this in production or protect it with authentication
 */
@RestController
@RequestMapping("/api/public/test/notifications")
@RequiredArgsConstructor
public class NotificationTestController {

    private final UserNotificationService userNotificationService;

    /**
     * Test endpoint - Send welcome email
     *
     * Example request:
     * POST http://localhost:8081/api/test/notifications/welcome
     * Body: {
     *   "userId": "user123",
     *   "email": "test@example.com",
     *   "username": "John Doe"
     * }
     */
    @PostMapping("/welcome")
    public ResponseEntity<NotificationResponse> testWelcomeEmail(
            @RequestBody WelcomeEmailRequest request) {

        NotificationResponse response = userNotificationService.sendWelcomeEmail(
                request.getUserId(),
                request.getEmail(),
                request.getUsername()
        );

        return ResponseEntity.ok(response);
    }






    // DTOs for test requests

    @lombok.Data
    public static class WelcomeEmailRequest {
        private String userId;
        private String email;
        private String username;
    }

    @lombok.Data
    public static class PushNotificationRequest {
        private String userId;
        private String deviceToken;
        private String message;
    }

    @lombok.Data
    public static class PasswordResetRequest {
        private String userId;
        private String email;
        private String resetToken;
    }
}