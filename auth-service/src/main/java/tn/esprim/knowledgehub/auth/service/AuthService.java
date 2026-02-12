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
import java.util.List;
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
        authUserRepository.findByEmail(email).ifPresent(authUser -> {

            resetRepository.deleteByAuthUser(authUser);

            String token = UUID.randomUUID().toString();

            Reset reset = Reset.builder()
                    .authUser(authUser)
                    .code(passwordEncoder.encode(token))
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            resetRepository.save(reset);

            notificationClient.sendNotification(
                    NotificationRequest.builder()
                            .userId(authUser.getId().toString())
                            .recipientEmail(email)
                            .type(NotificationType.EMAIL)
                            .subject("Reset password")
                            .content("Your reset code is: " + token)
                            .build()
            );
        });
    }


    @Transactional
    public AuthUser resetPassword(String token, String newPassword) {

        List<Reset> validResets =
                resetRepository.findByExpiresAtAfter(LocalDateTime.now());

        Reset reset = validResets.stream()
                .filter(r -> passwordEncoder.matches(token, r.getCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        AuthUser user = reset.getAuthUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        resetRepository.delete(reset);
        notificationClient.sendNotification(
                NotificationRequest.builder()
                        .userId(user.getId().toString())
                        .recipientEmail(user.getEmail())
                        .type(NotificationType.EMAIL)
                        .subject("Password reset successful")
                        .content("Your password has been reset successfully if you did not initiate this change, please contact administration ASAP")
                        .build()
        );
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