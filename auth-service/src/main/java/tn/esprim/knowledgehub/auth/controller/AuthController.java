package tn.esprim.knowledgehub.auth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.auth.dto.AuthRequest;
import tn.esprim.knowledgehub.auth.dto.AuthResponse;
import tn.esprim.knowledgehub.auth.dto.LoginRequest;
import tn.esprim.knowledgehub.auth.dto.RegisterRequest;
import tn.esprim.knowledgehub.auth.dto.TokenValidationResponse;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.service.AuthService;

import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
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
            @RequestBody AuthRequest request,

            @Parameter(
                    description = "User ID from authentication context",
                    required = false,
                    in = ParameterIn.HEADER,
                    schema = @Schema(type = "string")
            )
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        AuthUser authUser = authService.editPassword(Long.valueOf(userId), request.getPassword());
        return ResponseEntity.ok(AuthResponse.success(authUser, "User updated successfully"));
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
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.validateToken(token));
    }
}