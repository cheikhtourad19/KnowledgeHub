package tn.esprim.knowledgehub.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.auth.client.UserServiceClient;
import tn.esprim.knowledgehub.auth.dto.UserServiceRequest;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.repository.AuthUserRepository;

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
    private final UserServiceClient userServiceClient;

    @Transactional
    public List<AuthUser> processStudentsCsv(MultipartFile file) throws Exception {
        List<AuthUser> authUsers = new ArrayList<>();
        List<UserServiceRequest> userServiceRequests = new ArrayList<>();

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

                // Create AuthUser
                AuthUser authUser = AuthUser.builder()
                        .email(email)
                        .password(passwordEncoder.encode(email)) // Password is same as email
                        .role(AuthUser.Role.STUDENT)
                        .enabled(true)
                        .accountNonLocked(true)
                        .failedLoginAttempts(0)
                        .build();

                authUsers.add(authUser);
            }
        }catch (Exception e){
            throw e;
        }

        if (authUsers.isEmpty()) {
            log.warn("No valid students found in CSV file");
            return new ArrayList<>();
        }

        // Save all auth users and get their IDs
        List<AuthUser> savedAuthUsers = authUserRepository.saveAll(authUsers);
        log.info("Saved {} students in auth-service", savedAuthUsers.size());

        // Create UserServiceRequests with authId
        for (AuthUser savedAuthUser : savedAuthUsers) {

            // Parse the CSV line again to get nom and prenom
            String email = savedAuthUser.getEmail();
            String nom = "";
            String prenom = "";

            // Find matching email in the original list
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] fields = line.split(",");
                    if (fields.length >= 3 && fields[0].trim().equals(email)) {
                        nom = fields[1].trim();
                        prenom = fields[2].trim();
                        break;
                    }
                }
            }

            UserServiceRequest request = UserServiceRequest.builder()
                    .authId(String.valueOf(savedAuthUser.getId()))
                    .nom(nom)
                    .prenom(prenom)
                    .build();

            userServiceRequests.add(request);
        }

        // Call user-service to create students
        userServiceClient.createStudents(userServiceRequests);

        return savedAuthUsers;
    }

    @Transactional
    public List<AuthUser> processTeachersCsv(MultipartFile file) throws Exception {
        List<AuthUser> authUsers = new ArrayList<>();
        List<UserServiceRequest> userServiceRequests = new ArrayList<>();

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

                // Create AuthUser
                AuthUser authUser = AuthUser.builder()
                        .email(email)
                        .password(passwordEncoder.encode(email)) // Password is same as email
                        .role(AuthUser.Role.TEACHER)
                        .enabled(true)
                        .accountNonLocked(true)
                        .failedLoginAttempts(0)
                        .build();

                authUsers.add(authUser);
            }
        }

        if (authUsers.isEmpty()) {
            log.warn("No valid teachers found in CSV file");
            return new ArrayList<>();
        }

        // Save all auth users and get their IDs
        List<AuthUser> savedAuthUsers = authUserRepository.saveAll(authUsers);
        log.info("Saved {} teachers in auth-service", savedAuthUsers.size());

        // Create UserServiceRequests with authId
        for (AuthUser savedAuthUser : savedAuthUsers) {

            // Parse the CSV line again to get nom and prenom
            String email = savedAuthUser.getEmail();
            String nom = "";
            String prenom = "";

            // Find matching email in the original list
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] fields = line.split(",");
                    if (fields.length >= 3 && fields[0].trim().equals(email)) {
                        nom = fields[1].trim();
                        prenom = fields[2].trim();
                        break;
                    }
                }
            }

            UserServiceRequest request = UserServiceRequest.builder()
                    .authId(String.valueOf(savedAuthUser.getId()))
                    .nom(nom)
                    .prenom(prenom)
                    .build();

            userServiceRequests.add(request);
        }

        // Call user-service to create teachers
        userServiceClient.createTeachers(userServiceRequests);

        return savedAuthUsers;
    }
}
