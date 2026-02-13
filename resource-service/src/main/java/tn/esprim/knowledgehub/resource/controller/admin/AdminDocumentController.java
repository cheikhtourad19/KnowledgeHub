package tn.esprim.knowledgehub.resource.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.service.FileService;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@Slf4j
public class AdminDocumentController {

    private final FileService fileService;



    @Operation(summary = "Upload a file", description = "Upload a file to MinIO storage")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<tn.esprim.knowledgehub.resource.dto.response.ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "File type") @RequestParam("fileType") FileType fileType,
            @Parameter(description = "Bucket type (documents, pfa, pfe, stage)") @RequestParam("bucketType") String bucketType,
            @Parameter(description = "File title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "File description") @RequestParam(value = "description", required = false) String description,
            @RequestHeader(value = "X-Auth-Id") String authId,
            @RequestHeader(value = "X-User-Name") String userEmail
    ) {
        try {
            log.info("Uploading file: {} by user: {}", fileType, userEmail);

            FileUploadResponse response = fileService.uploadFile(
                    file,
                    fileType,
                    Long.parseLong(authId),
                    userEmail,
                    bucketType,
                    title,
                    description
            );

            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(response, "File uploaded successfully"));
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to upload file")
            );
        }
    }

    @Operation(summary = "Get Secure Preview Link", description = "Generates a 2-minute secure link for the frontend.")
    @GetMapping("/view/{fileId}")
    public ResponseEntity<?> getFileForView(
            @PathVariable Long fileId,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") String userId
    ) {
        try {

            File file = fileService.getFileById(fileId);

            boolean isAdmin = role.equals("ADMIN") ;
            boolean isOwner = file.getUploadedByUserId().equals(Long.parseLong(userId));

            if (!file.getIsPublished() && !isAdmin && !isOwner) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "This file is not yet published.")
                );
            }


            String presignedUrl = fileService.generatePresignedUrl(fileId, 2);

            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(presignedUrl, "Secure link generated"));
        } catch (Exception e) {
            log.error("Error generating view link: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to generate link")
            );
        }
    }


    @Operation(summary = "Get file metadata", description = "Get file metadata by ID")
    @GetMapping("/{fileId}")
    public ResponseEntity<tn.esprim.knowledgehub.resource.dto.response.ApiResponse<File>> getFileById(
            @Parameter(description = "File ID") @PathVariable Long fileId
    ) {
        try {
            File file = fileService.getFileById(fileId);
            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(file, "File retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to retrieve file")
            );
        }
    }



    @Operation(summary = "Delete file", description = "Delete a file from storage and database")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<tn.esprim.knowledgehub.resource.dto.response.ApiResponse<Void>> deleteFile(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") String userId
    ) {
        try {
            File file = fileService.getFileById(fileId);


            if (!role.equals("ADMIN") && !file.getUploadedByUserId().equals(Long.parseLong(userId))) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "Only admins and teachers can delete files")
                );
            }

            fileService.deleteFile(fileId);
            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(null, "File deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to delete file")
            );
        }
    }

    @Operation(summary = "Update publication status", description = "Publish or unpublish a file")
    @PutMapping("/{fileId}/publication")
    public ResponseEntity<tn.esprim.knowledgehub.resource.dto.response.ApiResponse<File>> updatePublicationStatus(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Parameter(description = "Publication status") @RequestParam Boolean isPublished,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        try {

            if (!role.equals("ADMIN") && !role.equals("TEACHER")) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "Only admins and teachers can update publication status")
                );
            }

            File file = fileService.updatePublicationStatus(fileId, isPublished);
            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(file, "Publication status updated successfully"));
        } catch (Exception e) {
            log.error("Error updating publication status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to update publication status")
            );
        }
    }

    @Operation(summary = "Update file metadata", description = "Update file title and description")
    @PutMapping("/{fileId}/metadata")
    public ResponseEntity<tn.esprim.knowledgehub.resource.dto.response.ApiResponse<File>> updateFileMetadata(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Parameter(description = "File title") @RequestParam(required = false) String title,
            @Parameter(description = "File description") @RequestParam(required = false) String description,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-Auth-Id") String authId
    ) {
        try {
            File file = fileService.getFileById(fileId);


            boolean isStaff = role.equals("ADMIN") || role.equals("TEACHER");
            boolean isOwner = file.getUploadedByUserId().equals(Long.parseLong(authId));

            if (!isStaff && !isOwner) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "You don't have permission to update this file")
                );
            }

            File updatedFile = fileService.updateFileMetadata(fileId, title, description);
            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(updatedFile, "File metadata updated successfully"));
        } catch (Exception e) {
            log.error("Error updating file metadata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to update file metadata")
            );
        }

    }
    }


