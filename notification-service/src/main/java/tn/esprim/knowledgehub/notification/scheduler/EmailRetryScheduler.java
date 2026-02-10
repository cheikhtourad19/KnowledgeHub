package tn.esprim.knowledgehub.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprim.knowledgehub.notification.entity.NotificationType;
import tn.esprim.knowledgehub.notification.entity.Notification;
import tn.esprim.knowledgehub.notification.repository.NotificationRepository;
import tn.esprim.knowledgehub.notification.service.NotificationService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;



    /**
     * Runs when the application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void sendUnsentEmailsOnStartup() {
        log.info("Application started - checking for unsent emails...");
        deleteUnsentEmails();
        retryUnsentEmails();
    }


    /**
        le hna koul sa3a
     */
     @Scheduled(cron = "0 0 * * * *")
     public void scheduledEmailRetryCron() {
         log.info("Scheduled task triggered - checking for unsent emails...");
         retryUnsentEmails();
     }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledEmailDeleteCron() {
        log.info("Scheduled task triggered - checking for unsent emails...");
            deleteUnsentEmails();
    }
     private void deleteUnsentEmails() {
         try {
             List<Notification> sentEmails = notificationRepository
                     .findByTypeAndEmailSent(
                             NotificationType.EMAIL, true);
             if (sentEmails.isEmpty()) {
                 return;
             }
             notificationRepository.deleteAll(sentEmails);

         }catch (Exception e) {
             log.error("Error during email retry task", e);
         }
     }

    private void retryUnsentEmails() {
        try {
            List<Notification> unsentEmails = notificationRepository
                    .findByTypeAndEmailSent(
                            NotificationType.EMAIL, false);

            if (unsentEmails.isEmpty()) {
                log.info("No unsent emails found");
                return;
            }

            log.info("Found {} unsent email(s) to retry", unsentEmails.size());

            for (Notification notification : unsentEmails) {
                try {
                    // Use async method to send email
                    notificationService.sendEmailNotificationAsync(notification.getId());
                    log.info("Queued email retry for notification: {}", notification.getId());
                } catch (Exception e) {
                    log.error("Failed to queue email retry for notification: {}", notification.getId(), e);
                }
            }

            log.info("Email retry task completed");
        } catch (Exception e) {
            log.error("Error during email retry task", e);
        }
    }
}