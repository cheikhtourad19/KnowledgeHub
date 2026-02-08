package tn.esprim.knowledgehub.notification.service;

import tn.esprim.knowledgehub.notification.dto.request.NotificationRequest;
import tn.esprim.knowledgehub.notification.dto.response.NotificationResponse;
import tn.esprim.knowledgehub.notification.entity.Notification;
import tn.esprim.knowledgehub.notification.entity.NotificationType;
import tn.esprim.knowledgehub.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Sending notification to user: {}", request.getUserId());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .content(request.getContent())
                .type(request.getType())
                .subject(request.getSubject())
                .recipientEmail(request.getRecipientEmail())
                .deviceToken(request.getDeviceToken())
                .delivered(false)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();


        boolean sent = sendActualNotification(notification);

        if (sent) {
            notification.setDelivered(true);
            notification.setDeliveredAt(LocalDateTime.now());
        }

        Notification saved = notificationRepository.save(notification);

        return mapToResponse(saved);
    }

    private boolean sendActualNotification(Notification notification) {
        /**
         * houn bach n integri system de notfication (email, push, etc.) w n handle les erreurs li ymkn t9a3 f had l process
         */

        if (notification.getType() == NotificationType.EMAIL) {
            log.info("Sending email to: {}", notification.getRecipientEmail());
            // Email sending logic here
            return true;
        } else {
            log.info("Sending push notification to device: {}", notification.getDeviceToken());
            // Push notification logic here
            return true;
        }
    }

    public NotificationResponse markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);

        return mapToResponse(updated);
    }

    public List<NotificationResponse> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndRead(userId, false)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .content(notification.getContent())
                .type(notification.getType())
                .delivered(notification.isDelivered())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .deliveredAt(notification.getDeliveredAt())
                .readAt(notification.getReadAt())
                .subject(notification.getSubject())
                .build();
    }
}
