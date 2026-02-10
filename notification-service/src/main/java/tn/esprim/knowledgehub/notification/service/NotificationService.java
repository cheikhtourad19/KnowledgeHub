package tn.esprim.knowledgehub.notification.service;

import org.springframework.scheduling.annotation.Async;
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
    private final EmailService emailService;

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
                .emailSent(false)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();


        Notification saved = notificationRepository.save(notification);

        // Send asynchronously - don't wait for result
        if (saved.getType() == NotificationType.EMAIL) {
            sendEmailNotificationAsync(saved.getId());
        } else if (saved.getType() == NotificationType.PUSH) {
            sendPushNotification(saved);
        }

        // Return immediately
        return mapToResponse(saved);
    }

    @Async("emailTaskExecutor")
    public void sendEmailNotificationAsync(String notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

            log.info("Async sending email to: {}", notification.getRecipientEmail());

            if (notification.getRecipientEmail() == null || notification.getRecipientEmail().isBlank()) {
                log.error("Recipient email is null or empty for notification: {}", notificationId);
                return;
            }

            if (notification.getSubject() == null || notification.getSubject().isBlank()) {
                notification.setSubject("KnowledgeHub Notification");
            }

            String htmlContent = emailService.buildHtmlTemplate(
                    formatEmailContent(notification.getContent()),
                    notification.getSubject()
            );

            // Send email - this will throw exception if it fails
            emailService.sendHtmlEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    htmlContent
            );

            // ✅ ONLY mark as sent/delivered if sendHtmlEmail() succeeded (no exception)
            notification.setEmailSent(true);
            notification.setDelivered(true);
            notification.setDeliveredAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("✅ Email sent and delivered successfully to: {}", notification.getRecipientEmail());

        } catch (Exception e) {
            log.error("Async email sending failed for notification: {}", notificationId, e);

            // Update retry count
            notificationRepository.findById(notificationId).ifPresent(n -> {
                n.setRetryCount(n.getRetryCount() + 1);
                n.setLastRetryAt(LocalDateTime.now());
                notificationRepository.save(n);
            });
        }
    }

    /**
     * Format email content with proper HTML paragraphs
     */
    private String formatEmailContent(String content) {
        if (content == null) {
            return "";
        }

        // Convert line breaks to HTML paragraphs
        String[] paragraphs = content.split("\n");
        StringBuilder html = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (!paragraph.trim().isEmpty()) {
                html.append("<p>").append(paragraph.trim()).append("</p>");
            }
        }

        return html.toString();
    }

    /**
     * l hna bach nchouf 7al l notification mta3 l mobile
     */
    private void sendPushNotification(Notification notification) {
        try {
            log.info("Sending push notification to device: {}", notification.getDeviceToken());

            // Validate device token
            if (notification.getDeviceToken() == null || notification.getDeviceToken().isBlank()) {
                log.error("Device token is null or empty");
                return;
            }

            // TODO: bach nchoufou m3a firebase wala provider a5ar

            log.info("Push notification would be sent: {}", notification.getContent());


        } catch (Exception e) {
            log.error("Failed to send push notification", e);
        }
    }

    public NotificationResponse markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

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