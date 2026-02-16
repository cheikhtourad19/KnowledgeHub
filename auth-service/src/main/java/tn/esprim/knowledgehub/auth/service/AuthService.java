package tn.esprim.knowledgehub.auth.service;


import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprim.knowledgehub.auth.client.UserServiceClient;
import tn.esprim.knowledgehub.auth.dto.*;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.entity.Reset;
import tn.esprim.knowledgehub.auth.exception.AuthenticationException;
import tn.esprim.knowledgehub.auth.exception.InvalidTokenException;
import tn.esprim.knowledgehub.auth.repository.AuthUserRepository;
import tn.esprim.knowledgehub.auth.repository.ResetRepository;
import tn.esprim.knowledgehub.auth.util.JwtUtil;
import tn.esprim.knowledgehub.auth.util.PasswordGenerator;
import tn.esprim.knowledgehub.auth.client.UserServiceClient;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserProfileService userProfileService;
    private final NotificationClient notificationClient;
    private final ResetRepository resetRepository;
    private final UserServiceClient userServiceClient;


    @Transactional
    public AuthResponse<Object> login(AuthRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());

        AuthUser authUser = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!authUser.getAccountNonLocked()) {
            return AuthResponse.error("Account is locked due to multiple failed login attempts", HttpStatus.FORBIDDEN);
        }

        if (!authUser.getEnabled()) {
            return AuthResponse.error("Account is disabled", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), authUser.getPassword())) {
            handleFailedLogin(authUser);
            return AuthResponse.error("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (authUser.getFailedLoginAttempts() > 0) {
            authUser.setFailedLoginAttempts(0);
        }

        authUser.setLastLogin(LocalDateTime.now());
        authUserRepository.save(authUser);

        // Fetch userId from user-service based on role
        Long userId = null;
        if (authUser.getRole() == AuthUser.Role.STUDENT) {
            userId = userServiceClient.getStudentIdByAuthId(authUser.getId().toString());
        } else if (authUser.getRole() == AuthUser.Role.TEACHER) {
            userId = userServiceClient.getTeacherIdByAuthId(authUser.getId().toString());
        }
        else {
            userId=userServiceClient.getAdminIdByAuthId(authUser.getId().toString());
        }

        String token = jwtUtil.generateToken(
                authUser.getUsername(),
                authUser.getId().toString(),
                userId != null ? userId.toString() : null,
                authUser.getRole().name()
        );

        return AuthResponse.authSuccess(token, authUser.getUsername(), authUser.getEmail(), authUser.getRole(), authUser.getId(), userId);
    }

    @Transactional
    public AuthResponse<AuthUser> registerUser(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getEmail());

        // Check if user already exists
        if (authUserRepository.findByEmail(request.getEmail()).isPresent()) {
            return AuthResponse.error("User with this email already exists", HttpStatus.CONFLICT);
        }

        // Create auth user
        AuthUser authUser = AuthUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : AuthUser.Role.STUDENT)
                .enabled(true)
                .accountNonLocked(true)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        authUser = authUserRepository.save(authUser);
        log.info("Auth user created successfully with ID: {}", authUser.getId());

        // Create user profile asynchronously - don't wait for completion
        if (request.getRole() == AuthUser.Role.STUDENT) {
            userProfileService.createStudentProfileAsync(
                    authUser.getId().toString(),
                    authUser.getEmail(),
                    request.getPrenom(),
                    request.getNom()
            );
            log.info("Student profile creation queued for authId: {}", authUser.getId());
        } else if (request.getRole() == AuthUser.Role.TEACHER) {
            userProfileService.createTeacherProfileAsync(
                    authUser.getId().toString(),
                    authUser.getEmail(),
                    request.getPrenom(),
                    request.getNom()
            );
            log.info("Teacher profile creation queued for authId: {}", authUser.getId());
        }

        return AuthResponse.success(authUser, "User registered successfully");
    }



    public AuthUser editPassword(Long id, String password){
        AuthUser authUser = authUserRepository.findById(id).orElseThrow(()->new RuntimeException("User not found with id: "+id));
        authUser.setPassword(passwordEncoder.encode(password));
        return authUserRepository.save(authUser);
    }

    @Transactional
    public void createResetToken(String email) {
        log.info("Creating reset token for email: {}", email);

        authUserRepository.findByEmail(email).ifPresentOrElse(authUser -> {
            log.info("User found for email: {}, User ID: {}", email, authUser.getId());

            try {
                resetRepository.deleteByAuthUser(authUser);
                log.info("Deleted existing reset tokens for user: {}", authUser.getId());

                String token = UUID.randomUUID().toString();
                LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

                log.info("Generated token: {}, Expires at: {}", token, expiryTime);

                Reset reset = Reset.builder()
                        .authUser(authUser)
                        .code(token)
                        .expiresAt(expiryTime)
                        .build();

                Reset savedReset = resetRepository.save(reset);
                log.info("Reset token saved with ID: {}, Code: {}, ExpiresAt: {}",
                    savedReset.getId(), savedReset.getCode(), savedReset.getExpiresAt());

                notificationClient.sendNotification(
                        NotificationRequest.builder()
                                .userId(authUser.getId().toString())
                                .recipientEmail(email)
                                .type(NotificationType.EMAIL)
                                .subject("Reset password")
                                .content("Your reset code is: " + token + ". This code will expire in 15 minutes.")
                                .build()
                );
                log.info("Reset notification sent to: {}", email);
            } catch (Exception e) {
                log.error("Error creating reset token for user: {}", authUser.getId(), e);
                throw new RuntimeException("Failed to create reset token", e);
            }
        }, () -> {
            log.warn("No user found for email: {}", email);
        });
    }

    public boolean checkIfValidResetToken(String token) {

        return resetRepository.findByCodeAndExpiresAtAfter(token, LocalDateTime.now()).isPresent();
    }

    public VerifyResetResponse verifyResetToken(String email, String token) {
        log.info("Verifying reset token for email: {}", email);
        log.debug("Token received: {}", token);

        if (email == null || email.trim().isEmpty() || token == null || token.trim().isEmpty()) {
            log.warn("Empty or null email/token provided");
            return VerifyResetResponse.builder()
                    .valid(false)
                    .token(null)
                    .message("Email and token are required")
                    .build();
        }

        String trimmedEmail = email.trim();
        String trimmedToken = token.trim();

        log.debug("Trimmed email: {}, Trimmed token: {}", trimmedEmail, trimmedToken);

        // Find user by email
        Optional<AuthUser> userOpt = authUserRepository.findByEmail(trimmedEmail);

        if (userOpt.isEmpty()) {
            log.warn("User not found for email: {}", trimmedEmail);
            return VerifyResetResponse.builder()
                    .valid(false)
                    .token(null)
                    .message("Invalid email or token")
                    .build();
        }

        AuthUser user = userOpt.get();
        log.info("User found with ID: {}", user.getId());

        // Find valid reset token
        LocalDateTime now = LocalDateTime.now();
        log.debug("Current time: {}", now);

        Optional<Reset> resetOpt = resetRepository.findByCodeAndExpiresAtAfter(
                trimmedToken,
                now
        );

        if (resetOpt.isEmpty()) {
            log.warn("Reset token not found or expired for token: {}", trimmedToken);

            // Check if token exists at all (without expiry check)
            List<Reset> anyResets = resetRepository.findByCode(trimmedToken);
            if (!anyResets.isEmpty()) {
                Reset r = anyResets.get(0);
                log.error("Token exists but is expired. Token: {}, ExpiresAt: {}, Now: {}",
                    trimmedToken, r.getExpiresAt(), now);
            } else {
                log.error("Token does not exist in database: {}", trimmedToken);
            }

            return VerifyResetResponse.builder()
                    .valid(false)
                    .token(null)
                    .message("Invalid or expired token")
                    .build();
        }

        Reset reset = resetOpt.get();
        log.info("Reset token found. User ID: {}, Expires at: {}", reset.getAuthUser().getId(), reset.getExpiresAt());

        // Verify the reset belongs to this user
        if (!reset.getAuthUser().getId().equals(user.getId())) {
            log.warn("Reset token does not belong to user. Expected user ID: {}, Token user ID: {}",
                user.getId(), reset.getAuthUser().getId());
            return VerifyResetResponse.builder()
                    .valid(false)
                    .token(null)
                    .message("Invalid token")
                    .build();
        }

        log.info("Reset token verified successfully for email: {}", trimmedEmail);
        return VerifyResetResponse.builder()
                .valid(true)
                .token(trimmedToken)
                .message("Token verified successfully")
                .build();
    }
    @Transactional
    public AuthUser resetPassword(String token, String newPassword) {
        log.info("=== RESET PASSWORD DEBUG START ===");
        log.info("Received token: '{}'", token);
        log.info("Token length: {}", token != null ? token.length() : "null");
        log.info("Current time: {}", LocalDateTime.now());

        // Trim the token
        String trimmedToken = token != null ? token.trim() : null;
        log.info("Trimmed token: '{}'", trimmedToken);

        // Check all resets in database
        List<Reset> allResets = resetRepository.findAll();
        log.info("Total resets in database: {}", allResets.size());

        allResets.forEach(r -> {
            log.info("DB Reset - Code: '{}', ExpiresAt: {}, Expired: {}, User: {}",
                    r.getCode(),
                    r.getExpiresAt(),
                    r.getExpiresAt().isBefore(LocalDateTime.now()),
                    r.getAuthUser().getEmail());
        });

        // Try to find the reset token
        Optional<Reset> resetOpt = resetRepository.findByCodeAndExpiresAtAfter(trimmedToken, LocalDateTime.now());
        log.info("Found matching reset: {}", resetOpt.isPresent());

        if (resetOpt.isEmpty()) {
            log.error("Invalid or expired token used for password reset");

            // Additional debug: check if token exists but expired
            List<Reset> expiredResets = resetRepository.findByCode(trimmedToken);
            if (!expiredResets.isEmpty()) {
                log.error("Token exists but is EXPIRED. Expiry time was: {}", expiredResets.get(0).getExpiresAt());
            } else {
                log.error("Token does NOT exist in database at all");
            }

            throw new RuntimeException("Invalid or expired token");
        }

        Reset reset = resetOpt.get();
        log.info("Found valid reset token for user: {}", reset.getAuthUser().getEmail());

        AuthUser user = reset.getAuthUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        resetRepository.delete(reset);
        log.info("Reset token deleted after successful password reset");

        notificationClient.sendNotification(
                NotificationRequest.builder()
                        .userId(user.getId().toString())
                        .recipientEmail(user.getEmail())
                        .type(NotificationType.EMAIL)
                        .subject("Password reset successful")
                        .content("Your password has been reset successfully. If you did not initiate this change, please contact administration ASAP.")
                        .build()
        );

        log.info("=== RESET PASSWORD DEBUG END ===");
        return authUserRepository.save(user);
    }



    public TokenValidationResponse validateToken(String token) {
        try {
            Claims claims = jwtUtil.validateToken(token);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .username(claims.getSubject())
                    .authId(claims.get("authId", String.class))
                    .userId(claims.get("userId", String.class))
                    .role(claims.get("role", String.class))
                    .build();
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired token");
        }
    }

    private void handleFailedLogin(AuthUser authUser) {
        int attempts = authUser.getFailedLoginAttempts() + 1;
        authUser.setFailedLoginAttempts(attempts);

        // Lock account after 20 failed attempts
        if (attempts >= 20) {
            authUser.setAccountNonLocked(false);
            log.warn("Account locked due to multiple failed login attempts: {}", authUser.getUsername());
        }

        authUserRepository.save(authUser);
    }
}