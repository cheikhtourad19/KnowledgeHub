package com.knowledgehub.notification.client;

import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    private final RestTemplate notificationRestTemplate;

    @Value("${notification.service.url:http://localhost:8081}")
    private String notificationServiceUrl;

    private static final String SEND_ENDPOINT = "/api/notifications/send";
    private static final String MARK_READ_ENDPOINT = "/api/notifications/{id}/read";
    private static final String USER_NOTIFICATIONS_ENDPOINT = "/api/notifications/user/{userId}";
    private static final String UNREAD_NOTIFICATIONS_ENDPOINT = "/api/notifications/user/{userId}/unread";

    public NotificationResponse sendNotification(NotificationRequest request) {
        try {
            String url = notificationServiceUrl + SEND_ENDPOINT;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<NotificationResponse> response = notificationRestTemplate.postForEntity(
                    url, entity, NotificationResponse.class);

            log.info("Notification sent successfully: {}", response.getBody().getId());
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    public NotificationResponse markAsRead(String notificationId) {
        try {
            String url = notificationServiceUrl + MARK_READ_ENDPOINT;
            ResponseEntity<NotificationResponse> response = notificationRestTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PUT,
                    null,
                    NotificationResponse.class,
                    notificationId);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to mark notification as read: {}", notificationId, e);
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }

    public List<NotificationResponse> getUserNotifications(String userId) {
        try {
            String url = notificationServiceUrl + USER_NOTIFICATIONS_ENDPOINT;
            ResponseEntity<NotificationResponse[]> response = notificationRestTemplate.getForEntity(
                    url, NotificationResponse[].class, userId);

            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get user notifications for userId: {}", userId, e);
            throw new RuntimeException("Failed to get user notifications", e);
        }
    }

    public List<NotificationResponse> getUnreadNotifications(String userId) {
        try {
            String url = notificationServiceUrl + UNREAD_NOTIFICATIONS_ENDPOINT;
            ResponseEntity<NotificationResponse[]> response = notificationRestTemplate.getForEntity(
                    url, NotificationResponse[].class, userId);

            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get unread notifications for userId: {}", userId, e);
            throw new RuntimeException("Failed to get unread notifications", e);
        }
    }
}
