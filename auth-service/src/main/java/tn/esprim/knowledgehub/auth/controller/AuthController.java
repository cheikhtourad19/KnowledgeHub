package tn.esprim.knowledgehub.auth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.auth.dto.*;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.service.AuthService;

import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication, registration, and token management")
public class AuthController {

    private final AuthService authService;


    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns authentication tokens.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            )
    })
    @PostMapping("/public/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {


        AuthResponse response = authService.login(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user in both auth service and user service.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthResponse.error("Only admins can register users", HttpStatus.FORBIDDEN));
        }
        AuthResponse response = authService.registerUser(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }





    @Operation(
            summary = "Update user password",
            description = "Updates the password for the authenticated user.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New password",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequest.class)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Password does not meet requirements",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            )
    })
    @PutMapping("/edit-password")
    public ResponseEntity<AuthResponse> editPassword(
            @RequestBody EditPasswordRequest request,

            @Parameter(
                    description = "User ID from authentication context",
                    required = false,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string")
            )
            @RequestHeader(value = "X-Auth-Id", required = false) String userId) {
        AuthUser user = null;
        try {
           user = authService.editUserPassword(Long.valueOf(userId), request.getOldPassword(), request.getNewPassword());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(AuthResponse.error("invalid credentials", HttpStatus.BAD_REQUEST));
        }
        return ResponseEntity.ok(AuthResponse.success(user, "User updated successfully"));
    }

    @Operation(
            summary = "Validate authentication token",
            description = "Validates a JWT token and returns user information if valid.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "Bearer token in format: 'Bearer {token}'",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")
                    )
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenValidationResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token is invalid or expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenValidationResponse.class)
                    )
            )
    })
    @PostMapping("/public/reset-password")
    public ResponseEntity<AuthResponse> requestReset(
            @RequestBody CreateResetRequest request
    ) {
        authService.createResetToken(request.getEmail());

        return ResponseEntity.ok(
                AuthResponse.success(
                        null,
                        "If the email exists, a reset link has been sent"
                )
        );
    }

    @PostMapping("/public/verify-reset-token")
    public ResponseEntity<VerifyResetResponse> verifyResetToken(
            @Valid @RequestBody VerifyResetRequest request
    ) {
        log.info("Verifying reset token for email: {}", request.getEmail());

        try {
            VerifyResetResponse response = authService.verifyResetToken(request.getEmail(), request.getToken());

            if (response.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error verifying reset token", e);
            return ResponseEntity.badRequest().body(
                    VerifyResetResponse.builder()
                            .valid(false)
                            .token(null)
                            .message("Error verifying token")
                            .build()
            );
        }
    }

    @GetMapping("/public/reset-password/{token}")
    public ResponseEntity<AuthResponse> checkResetToken(@PathVariable String token){
        boolean isValid = this.authService.checkIfValidResetToken(token);
        if(isValid){
            return ResponseEntity.ok(AuthResponse.success(null,"Valid token"));
        }else{
            return ResponseEntity.badRequest().body(AuthResponse.error("Invalid token",HttpStatus.BAD_REQUEST));
        }
    }
    @PostMapping("/public/reset-password/{token}")
    public ResponseEntity<AuthResponse> resetPassword(
            @PathVariable String token,
            @Valid @RequestBody ResetRequest request
    ) {
        log.info("Attempting to reset password with token");

        try {
            authService.resetPassword(token, request.getNewPassword());
            return ResponseEntity.ok(
                    AuthResponse.success(null,"Password reset successfully")
            );
        } catch (RuntimeException e) {
            log.error("Error resetting password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    AuthResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST)
            );
        } catch (Exception e) {
            log.error("Unexpected error resetting password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error("An error occurred while resetting password", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @Operation(
            summary = "Verify user password",
            description = "Verifies if the provided password matches the authenticated user's password"
    )
    @PostMapping("/verify-password")
    public ResponseEntity<AuthResponse> verifyPassword(
            @RequestParam String password,
            @RequestHeader(value = "X-Auth-Id") String authId
    ) {
        try {
            boolean isValid = authService.verifyPassword(authId, password);

            if (isValid) {
                return ResponseEntity.ok(
                        AuthResponse.success(null, "Password verified successfully")
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("Invalid password", HttpStatus.UNAUTHORIZED)
                );
            }
        } catch (Exception e) {
            log.error("Error verifying password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error("Error verifying password", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<AuthResponse> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (!Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthResponse.error("Only admins can delete users", HttpStatus.FORBIDDEN));
        }

        authService.deleteUser(id);

        return ResponseEntity.ok(AuthResponse.success(null, "User deleted successfully"));
    }
}