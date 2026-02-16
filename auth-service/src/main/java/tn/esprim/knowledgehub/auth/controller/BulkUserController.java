package tn.esprim.knowledgehub.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.auth.dto.AuthResponse;
import tn.esprim.knowledgehub.auth.dto.BulkUserCreationResponse;
import tn.esprim.knowledgehub.auth.service.BulkUserService;

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
            BulkUserCreationResponse response = bulkUserService.processStudentsCsv(file);
            return ResponseEntity.ok(AuthResponse.success(response,"Users created and notifications sent successfully"));
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
            BulkUserCreationResponse response = bulkUserService.processTeachersCsv(file);
            return ResponseEntity.ok(
                    AuthResponse.success(response,"Users created and notifications sent successfully")
            );
        } catch (Exception e) {
            log.error("Error processing teachers CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Error processing file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
