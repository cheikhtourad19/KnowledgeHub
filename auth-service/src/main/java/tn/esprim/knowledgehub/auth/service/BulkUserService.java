package tn.esprim.knowledgehub.auth.service;

import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.auth.dto.BulkUserCreationResult;
import tn.esprim.knowledgehub.auth.dto.UserServiceRequest;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.repository.AuthUserRepository;
import tn.esprim.knowledgehub.auth.util.PasswordGenerator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkUserService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;
    private final NotificationClient notificationClient;

    @Transactional
    public List<BulkUserCreationResult> processStudentsCsv(MultipartFile file) throws Exception {
        // Temporary class to hold all parsed data together
        class StudentData {
            String email;
            String nom;
            String prenom;
            String password;
        }

        List<StudentData> validStudents = new ArrayList<>();
        List<String> allEmails = new ArrayList<>();

        // STEP 1: Parse CSV ONCE and collect all emails first
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }

                String[] fields = line.split(",");
                if (fields.length < 3) {
                    log.warn("Skipping invalid line {}: {}", lineNumber, line);
                    continue;
                }

                String email = fields[0].trim();
                String nom = fields[1].trim();
                String prenom = fields[2].trim();

                // Validate email format
                if (!email.contains("@")) {
                    log.warn("Skipping line {} - invalid email: {}", lineNumber, email);
                    continue;
                }

                allEmails.add(email);

                // Store valid student data (will filter duplicates later)
                StudentData data = new StudentData();
                data.email = email;
                data.nom = nom;
                data.prenom = prenom;
                data.password = PasswordGenerator.generatePassword(8);

                validStudents.add(data);
            }
        } catch (Exception e) {
            throw e;
        }

        // STEP 1.5: Batch check for existing emails (ONE query instead of N queries)
        List<String> existingEmails = authUserRepository.findEmailsByEmailIn(allEmails);
        validStudents.removeIf(data -> {
            if (existingEmails.contains(data.email)) {
                log.warn("Skipping email already exists: {}", data.email);
                return true;
            }
            return false;
        });

        if (validStudents.isEmpty()) {
            log.warn("No valid students found in CSV file");
            return new ArrayList<>();
        }

        // STEP 2: Create and save all AuthUsers at once
        List<AuthUser> authUsers = new ArrayList<>();
        for (StudentData data : validStudents) {
            AuthUser authUser = AuthUser.builder()
                    .email(data.email)
                    .password(passwordEncoder.encode(data.password))
                    .role(AuthUser.Role.STUDENT)
                    .enabled(true)
                    .accountNonLocked(true)
                    .failedLoginAttempts(0)
                    .build();
            authUsers.add(authUser);
        }

        List<AuthUser> savedAuthUsers = authUserRepository.saveAll(authUsers);
        log.info("Saved {} students in auth-service", savedAuthUsers.size());

        // STEP 3: Prepare UserServiceRequests and send notifications ASYNCHRONOUSLY
        List<UserServiceRequest> userServiceRequests = new ArrayList<>();
        List<BulkUserCreationResult> results = new ArrayList<>();

        for (int i = 0; i < savedAuthUsers.size(); i++) {
            AuthUser savedAuthUser = savedAuthUsers.get(i);
            StudentData data = validStudents.get(i);

            // Prepare user service request
            UserServiceRequest userRequest = UserServiceRequest.builder()
                    .authId(String.valueOf(savedAuthUser.getId()))
                    .nom(data.nom)
                    .prenom(data.prenom)
                    .build();
            userServiceRequests.add(userRequest);

            // Send notification ASYNCHRONOUSLY (fire and forget - no waiting!)
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(String.valueOf(savedAuthUser.getId()))
                    .subject("Bienvenue sur KnowledgeHub")
                    .content("Bienvenue sur KnowledgeHub, " + data.prenom + " " + data.nom + "! Votre mot de passe est: " + data.password)
                    .type(NotificationType.EMAIL)
                    .recipientEmail(data.email)
                    .build();

            // Use ASYNC method - doesn't block!
            boolean notified = false;
            try {
                notificationClient.sendNotificationAsync(notificationRequest);
                notified = true;
                log.debug("Notification queued for student: {}", data.email);
            } catch (Exception e) {
                log.error("Failed to queue notification for student: {}", data.email, e);
            }

            // Create simplified result object
            BulkUserCreationResult result = BulkUserCreationResult.builder()
                    .email(data.email)
                    .nom(data.nom)
                    .prenom(data.prenom)
                    .notified(notified)
                    .build();
            results.add(result);
        }

        // STEP 4: Call user-service asynchronously to create all profiles in one batch
        userProfileService.createStudentProfilesAsync(userServiceRequests);
        log.info("Queued {} student profiles for creation", userServiceRequests.size());

        return results;
    }

    @Transactional
    public List<BulkUserCreationResult> processTeachersCsv(MultipartFile file) throws Exception {
        // Temporary class to hold all parsed data together
        class TeacherData {
            String email;
            String nom;
            String prenom;
            String password;
        }

        List<TeacherData> validTeachers = new ArrayList<>();
        List<String> allEmails = new ArrayList<>();

        // STEP 1: Parse CSV ONCE and collect all emails first
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }

                String[] fields = line.split(",");
                if (fields.length < 3) {
                    log.warn("Skipping invalid line {}: {}", lineNumber, line);
                    continue;
                }

                String email = fields[0].trim();
                String nom = fields[1].trim();
                String prenom = fields[2].trim();

                // Validate email format
                if (!email.contains("@")) {
                    log.warn("Skipping line {} - invalid email: {}", lineNumber, email);
                    continue;
                }

                allEmails.add(email);

                // Store valid teacher data (will filter duplicates later)
                TeacherData data = new TeacherData();
                data.email = email;
                data.nom = nom;
                data.prenom = prenom;
                data.password = PasswordGenerator.generatePassword(8);

                validTeachers.add(data);
            }
        }

        // STEP 1.5: Batch check for existing emails (ONE query instead of N queries)
        List<String> existingEmails = authUserRepository.findEmailsByEmailIn(allEmails);
        validTeachers.removeIf(data -> {
            if (existingEmails.contains(data.email)) {
                log.warn("Skipping email already exists: {}", data.email);
                return true;
            }
            return false;
        });

        if (validTeachers.isEmpty()) {
            log.warn("No valid teachers found in CSV file");
            return new ArrayList<>();
        }

        // STEP 2: Create and save all AuthUsers at once
        List<AuthUser> authUsers = new ArrayList<>();
        for (TeacherData data : validTeachers) {
            AuthUser authUser = AuthUser.builder()
                    .email(data.email)
                    .password(passwordEncoder.encode(data.password))
                    .role(AuthUser.Role.TEACHER)
                    .enabled(true)
                    .accountNonLocked(true)
                    .failedLoginAttempts(0)
                    .build();
            authUsers.add(authUser);
        }

        List<AuthUser> savedAuthUsers = authUserRepository.saveAll(authUsers);
        log.info("Saved {} teachers in auth-service", savedAuthUsers.size());

        // STEP 3: Prepare UserServiceRequests and send notifications ASYNCHRONOUSLY
        List<UserServiceRequest> userServiceRequests = new ArrayList<>();
        List<BulkUserCreationResult> results = new ArrayList<>();

        for (int i = 0; i < savedAuthUsers.size(); i++) {
            AuthUser savedAuthUser = savedAuthUsers.get(i);
            TeacherData data = validTeachers.get(i);

            // Prepare user service request
            UserServiceRequest userRequest = UserServiceRequest.builder()
                    .authId(String.valueOf(savedAuthUser.getId()))
                    .nom(data.nom)
                    .prenom(data.prenom)
                    .build();
            userServiceRequests.add(userRequest);

            // Send notification ASYNCHRONOUSLY (fire and forget - no waiting!)
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(String.valueOf(savedAuthUser.getId()))
                    .subject("Bienvenue sur KnowledgeHub")
                    .content("Bienvenue sur KnowledgeHub, " + data.prenom + " " + data.nom + "! Votre mot de passe est: " + data.password)
                    .type(NotificationType.EMAIL)
                    .recipientEmail(data.email)
                    .build();

            // Use ASYNC method - doesn't block!
            boolean notified = false;
            try {
                notificationClient.sendNotificationAsync(notificationRequest);
                notified = true;
                log.debug("Notification queued for teacher: {}", data.email);
            } catch (Exception e) {
                log.error("Failed to queue notification for teacher: {}", data.email, e);
            }

            // Create simplified result object
            BulkUserCreationResult result = BulkUserCreationResult.builder()
                    .email(data.email)
                    .nom(data.nom)
                    .prenom(data.prenom)
                    .notified(notified)
                    .build();
            results.add(result);
        }

        // STEP 4: Call user-service asynchronously to create all profiles in one batch
        userProfileService.createTeacherProfilesAsync(userServiceRequests);
        log.info("Queued {} teacher profiles for creation", userServiceRequests.size());

        return results;
    }
}
