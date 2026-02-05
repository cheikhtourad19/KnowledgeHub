package tn.esprim.knowledgehub.auth.controller;

import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.auth.dto.AuthResponse;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.service.BulkUserService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class BulkUserController {

    private final BulkUserService bulkUserService;

    @PostMapping("/students/upload")
    public ResponseEntity<AuthResponse> uploadStudents(@RequestParam("file") MultipartFile file,@RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthResponse.error("Only admins can upload students", HttpStatus.FORBIDDEN));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("File is empty", HttpStatus.BAD_REQUEST));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Only CSV files are allowed", HttpStatus.BAD_REQUEST));
        }

        try {
            List<AuthUser> count = bulkUserService.processStudentsCsv(file);
            return ResponseEntity.ok(AuthResponse.success(count,"users saved"));
        } catch (Exception e) {
            log.error("Error processing students CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Error processing file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/teachers/upload")
    public ResponseEntity<AuthResponse> uploadTeachers(@RequestParam("file") MultipartFile file,@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AuthResponse.error("Only admins can upload teachers", HttpStatus.FORBIDDEN));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("File is empty", HttpStatus.BAD_REQUEST));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Only CSV files are allowed", HttpStatus.BAD_REQUEST));
        }

        try {
            List<AuthUser> auth = bulkUserService.processTeachersCsv(file);
            return ResponseEntity.ok(
                    AuthResponse.success(auth,"users created")
            );
        } catch (Exception e) {
            log.error("Error processing teachers CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Error processing file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
