package tn.esprim.knowledgehub.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.FileDownloadResponse;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.service.FileService;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "APIs for file upload, download, and management")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Upload a file", description = "Upload a file to MinIO storage")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "File type") @RequestParam("fileType") FileType fileType,
            @Parameter(description = "Bucket type (documents, pfa, pfe, stage)") @RequestParam("bucketType") String bucketType,
            @RequestHeader(value = "X-Auth-Id") String authId,
            @RequestHeader(value = "X-User-Name") String userEmail
    ) {
        try {
            log.info("Uploading file: {} by user: {}", file.getOriginalFilename(), userEmail);

            FileUploadResponse response = fileService.uploadFile(
                    file,
                    fileType,
                    Long.parseLong(authId),
                    userEmail,
                    bucketType
            );

            return ResponseEntity.ok(ApiResponse.success(response, "File uploaded successfully"));
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to upload file")
            );
        }
    }

    @Operation(summary = "Get Secure Preview Link", description = "Generates a 2-minute secure link for the frontend.")
    @GetMapping("/view/{fileId}")
    public ResponseEntity<?> getFileForView(
            @PathVariable Long fileId,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-Auth-Id") String authId
    ) {
        try {

            File file = fileService.getFileById(fileId);

            boolean isStaff = role.equals("ADMIN") || role.equals("TEACHER");
            boolean isOwner = file.getUploadedByUserId().equals(Long.parseLong(authId));

            if (!file.getIsPublished() && !isStaff && !isOwner) {
                return ResponseEntity.status(403).body(
                        ApiResponse.error("Forbidden", "This file is not yet published.")
                );
            }


            String presignedUrl = fileService.generatePresignedUrl(fileId, 2);

            return ResponseEntity.ok(ApiResponse.success(presignedUrl, "Secure link generated"));
        } catch (Exception e) {
            log.error("Error generating view link: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to generate link")
            );
        }
    }



    @Operation(summary = "Get presigned URL", description = "Generate a presigned URL for file download")
    @GetMapping("/{fileId}/presigned-url")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Parameter(description = "Expiry time in minutes") @RequestParam(defaultValue = "60") int expiryMinutes
    ) {
        try {
            String url = fileService.generatePresignedUrl(fileId, expiryMinutes);
            return ResponseEntity.ok(ApiResponse.success(url, "Presigned URL generated successfully"));
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to generate presigned URL")
            );
        }
    }

    @Operation(summary = "Get file metadata", description = "Get file metadata by ID")
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<File>> getFileById(
            @Parameter(description = "File ID") @PathVariable Long fileId
    ) {
        try {
            File file = fileService.getFileById(fileId);
            return ResponseEntity.ok(ApiResponse.success(file, "File retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to retrieve file")
            );
        }
    }



    @Operation(summary = "Delete file", description = "Delete a file from storage and database")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") String authId
    ) {
        try {
            File file = fileService.getFileById(fileId);


            if (!role.equals("ADMIN") && !file.getUploadedByUserId().equals(Long.parseLong(authId))) {
                return ResponseEntity.status(403).body(
                        ApiResponse.error("Forbidden", "Only admins and teachers can delete files")
                );
            }

            fileService.deleteFile(fileId);
            return ResponseEntity.ok(ApiResponse.success(null, "File deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to delete file")
            );
        }
    }

    @Operation(summary = "Update publication status", description = "Publish or unpublish a file")
    @PutMapping("/{fileId}/publication")
    public ResponseEntity<ApiResponse<File>> updatePublicationStatus(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Parameter(description = "Publication status") @RequestParam Boolean isPublished,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        try {
            // Check if user is admin or teacher
            if (!role.equals("ADMIN") && !role.equals("TEACHER")) {
                return ResponseEntity.status(403).body(
                        ApiResponse.error("Forbidden", "Only admins and teachers can update publication status")
                );
            }

            File file = fileService.updatePublicationStatus(fileId, isPublished);
            return ResponseEntity.ok(ApiResponse.success(file, "Publication status updated successfully"));
        } catch (Exception e) {
            log.error("Error updating publication status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to update publication status")
            );
        }
    }


}
