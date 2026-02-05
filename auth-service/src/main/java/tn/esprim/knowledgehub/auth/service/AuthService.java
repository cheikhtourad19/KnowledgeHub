package tn.esprim.knowledgehub.auth.service;


import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprim.knowledgehub.auth.dto.AuthRequest;
import tn.esprim.knowledgehub.auth.dto.AuthResponse;
import tn.esprim.knowledgehub.auth.dto.LoginRequest;
import tn.esprim.knowledgehub.auth.dto.TokenValidationResponse;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.exception.AuthenticationException;
import tn.esprim.knowledgehub.auth.exception.InvalidTokenException;
import tn.esprim.knowledgehub.auth.repository.AuthUserRepository;
import tn.esprim.knowledgehub.auth.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthUserRepository authUserRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;



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

        String token = jwtUtil.generateToken(
                authUser.getUsername(),
                authUser.getId().toString(),
                authUser.getRole().name()
        );

        return AuthResponse.authSuccess(token, authUser.getUsername(), authUser.getEmail(), authUser.getRole(), authUser.getId());
    }



    public AuthUser editPassword(Long id, String password){
        AuthUser authUser = authUserRepository.findById(id).orElseThrow(()->new RuntimeException("User not found with id: "+id));
        authUser.setPassword(passwordEncoder.encode(password));
        return authUserRepository.save(authUser);
    }


    public TokenValidationResponse validateToken(String token) {
        try {
            Claims claims = jwtUtil.validateToken(token);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .username(claims.getSubject())
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