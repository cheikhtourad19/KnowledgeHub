package tn.esprim.knowledgehub.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.entity.AuthUser.Role;
import tn.esprim.knowledgehub.auth.repository.AuthUserRepository;
import tn.esprim.knowledgehub.auth.service.UserProfileService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        createAdminUser();
        createTestUser();
    }

    private void createAdminUser() {
        if (authUserRepository.findByEmail(adminEmail).isEmpty()) {
            AuthUser admin = AuthUser.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .accountNonLocked(true)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            authUserRepository.save(admin);
            userProfileService.createAdminProfileAsync(String.valueOf(admin.getId()));
            log.info("Admin user created successfully");
        }
    }

    private void createTestUser() {
        if (authUserRepository.findByEmail("mohamedelgheith.cheikhtourad@esprim.tn").isEmpty()) {
            AuthUser testUser = AuthUser.builder()

                    .email("mohamedelgheith.cheikhtourad@esprim.tn")
                    .password(passwordEncoder.encode("12345678"))
                    .role(Role.STUDENT)
                    .enabled(true)
                    .accountNonLocked(true)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            authUserRepository.save(testUser);
            userProfileService.createStudentProfileAsync(
                    String.valueOf(testUser.getId()),
                    testUser.getEmail(),
                    "Mohamed Elgheith",
                    "Cheikh Tourad"
            );
            log.info("Test user created successfully");
        }
    }
}