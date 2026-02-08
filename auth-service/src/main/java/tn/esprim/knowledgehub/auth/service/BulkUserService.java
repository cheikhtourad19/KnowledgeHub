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
    public List<AuthUser> processStudentsCsv(MultipartFile file) throws Exception {
        // Temporary class to hold all parsed data together
        class StudentData {
            String email;
            String nom;
            String prenom;
            String password;
        }

        List<StudentData> validStudents = new ArrayList<>();

        // STEP 1: Parse CSV ONCE and validate all data
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

                // Validate email
                if (!email.contains("@")) {
                    log.warn("Skipping line {} - invalid email: {}", lineNumber, email);
                    continue;
                }

                // Check if email already exists
                if (authUserRepository.existsByEmail(email)) {
                    log.warn("Skipping line {} - email already exists: {}", lineNumber, email);
                    continue;
                }

                // Store valid student data
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

        // STEP 3: Prepare UserServiceRequests and NotificationRequests
        List<UserServiceRequest> userServiceRequests = new ArrayList<>();

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


            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(String.valueOf(savedAuthUser.getId()))
                    .subject("Bienvenue sur KnowledgeHub")
                    .content("Bienvenue sur KnowledgeHub, " + data.prenom + " " + data.nom + "! Votre mot de passe est: " + data.password)
                    .type(NotificationType.EMAIL)
                    .recipientEmail(data.email)
                    .build();
            notificationClient.sendNotificationAsync(notificationRequest);
        }

        // STEP 4: Call user-service asynchronously to create all profiles in one batch
        userProfileService.createStudentProfilesAsync(userServiceRequests);
        log.info("Queued {} student profiles for creation", userServiceRequests.size());

        return savedAuthUsers;
    }

    @Transactional
    public List<AuthUser> processTeachersCsv(MultipartFile file) throws Exception {
        // Temporary class to hold all parsed data together
        class TeacherData {
            String email;
            String nom;
            String prenom;
            String password;
        }

        List<TeacherData> validTeachers = new ArrayList<>();

        // STEP 1: Parse CSV ONCE and validate all data
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

                // Validate email
                if (!email.contains("@")) {
                    log.warn("Skipping line {} - invalid email: {}", lineNumber, email);
                    continue;
                }

                // Check if email already exists
                if (authUserRepository.existsByEmail(email)) {
                    log.warn("Skipping line {} - email already exists: {}", lineNumber, email);
                    continue;
                }

                // Store valid teacher data
                TeacherData data = new TeacherData();
                data.email = email;
                data.nom = nom;
                data.prenom = prenom;
                data.password = PasswordGenerator.generatePassword(8);

                validTeachers.add(data);
            }
        }

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

        // STEP 3: Prepare UserServiceRequests and NotificationRequests
        List<UserServiceRequest> userServiceRequests = new ArrayList<>();

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

            // Send welcome email asynchronously (fire and forget)
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(String.valueOf(savedAuthUser.getId()))
                    .subject("Bienvenue sur KnowledgeHub")
                    .content("Bienvenue sur KnowledgeHub, " + data.prenom + " " + data.nom + "! Votre mot de passe est: " + data.password)
                    .type(NotificationType.EMAIL)
                    .recipientEmail(data.email)
                    .build();
            notificationClient.sendNotificationAsync(notificationRequest);
        }

        // STEP 4: Call user-service asynchronously to create all profiles in one batch
        userProfileService.createTeacherProfilesAsync(userServiceRequests);
        log.info("Queued {} teacher profiles for creation", userServiceRequests.size());

        return savedAuthUsers;
    }
}
