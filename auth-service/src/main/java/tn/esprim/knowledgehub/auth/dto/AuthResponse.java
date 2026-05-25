package tn.esprim.knowledgehub.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import tn.esprim.knowledgehub.auth.entity.AuthUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse<T> {

    private boolean success;
    private String message;
    private HttpStatus status;
    private T data;
    private String error;

    // Auth-specific fields
    private String token;
    private String username;
    private String email;
    private AuthUser.Role role;
    private Long authId;  // ID from auth-service

    // Factory methods for common responses
    public static <T> AuthResponse<T> success(T data, String message) {
        return AuthResponse.<T>builder()
                .success(true)
                .message(message)
                .status(HttpStatus.OK)
                .data(data)
                .build();
    }

    public static <T> AuthResponse<T> error(String error, HttpStatus status) {
        return AuthResponse.<T>builder()
                .success(false)
                .error(error)
                .status(status)
                .build();
    }

    public static AuthResponse<Object> authSuccess(String token, String username, String email, AuthUser.Role role, Long authId) {
        return AuthResponse.<Object>builder()
                .success(true)
                .message("Authentication successful")
                .status(HttpStatus.OK)
                .token(token)
                .username(username)
                .email(email)
                .role(role)
                .authId(authId)
                .build();
    }
}
