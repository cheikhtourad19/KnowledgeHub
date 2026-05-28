package tn.esprim.knowledgehub.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.dto.response.TeacherCVResponse;
import tn.esprim.knowledgehub.user.service.TeacherCVService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/teachers/cv")
@RequiredArgsConstructor
public class TeacherCVController {

    private final TeacherCVService teacherCVService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadCV(
            @RequestHeader("X-Auth-Id") String authId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("File is empty")
                            .build());
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Only PDF files are accepted")
                            .build());
        }

        var cv = teacherCVService.uploadCV(file, authId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("CV uploaded and processed successfully")
                        .data(TeacherCVResponse.fromEntity(cv))
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCVs(@RequestHeader("X-Auth-Id") String authId) {
        List<TeacherCVResponse> cvs = teacherCVService.getTeacherCVs(authId)
                .stream()
                .map(TeacherCVResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("CVs retrieved successfully")
                        .data(cvs)
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveCV(@RequestHeader("X-Auth-Id") String authId) {
        try {
            var cv = teacherCVService.getActiveCV(authId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Active CV retrieved successfully")
                            .data(TeacherCVResponse.fromEntity(cv))
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/{cvId}")
    public ResponseEntity<ApiResponse> getCVById(@PathVariable Long cvId) {
        var cv = teacherCVService.getCVById(cvId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("CV retrieved successfully")
                        .data(TeacherCVResponse.fromEntity(cv))
                        .build()
        );
    }

    @DeleteMapping("/{cvId}")
    public ResponseEntity<ApiResponse> deleteCV(@PathVariable Long cvId) {
        teacherCVService.deleteCV(cvId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("CV deleted successfully")
                        .build()
        );
    }

    @GetMapping("/active/presigned-url")
    public ResponseEntity<ApiResponse> getPresignedUrlForActiveCV(
            @RequestHeader("X-Auth-Id") String authId) {
        try {
            var cv = teacherCVService.getActiveCV(authId);
            String url = teacherCVService.generatePresignedUrl(cv.getId(), 60);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Presigned URL generated successfully")
                            .data(url)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/{cvId}/presigned-url")
    public ResponseEntity<ApiResponse> getPresignedUrl(@PathVariable Long cvId) {
        try {
            String url = teacherCVService.generatePresignedUrl(cvId, 60);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Presigned URL generated successfully")
                            .data(url)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
}
