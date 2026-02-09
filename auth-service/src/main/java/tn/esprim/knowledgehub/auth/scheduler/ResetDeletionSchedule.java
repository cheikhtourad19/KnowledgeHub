package tn.esprim.knowledgehub.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprim.knowledgehub.auth.entity.Reset;
import tn.esprim.knowledgehub.auth.repository.ResetRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResetDeletionSchedule {

    @Autowired
    private final ResetRepository resetRepository;



    /**
     * Runs when the application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void DeleteExpiredResetsOnStartup() {
        log.info("Application started - checking for expired resets...");
        deleteResets();
    }


    /**
        le hna koul sa3a
     */
     @Scheduled(cron = "0 0 * * * *")
     public void scheduledCron() {
         log.info("Scheduled task triggered - checking for expired resets...");
         deleteResets();
     }

    private void deleteResets() {
        try {
            List<Reset> expiredResets = resetRepository.findByExpiresAtBefore(LocalDateTime.now());

            if (expiredResets.isEmpty()) {
                log.info("No unsent emails found");
                return;
            }

            log.info("Found {} unsent email(s) to retry", expiredResets.size());

            for (Reset reset : expiredResets) {

                    // Use async method to send email
                    resetRepository.delete(reset);
                    log.info("Deleted expired reset with id: {}", reset.getId());

            }


        } catch (Exception e) {
            log.error("Error during email retry task", e);
        }
    }
}