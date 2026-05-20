package tn.esprim.knowledgehub.resource.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.service.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@Slf4j
public class AdminDocumentController {

    private final FileService fileService;
    private final AIandSearchService aiandSearchService;
    private final AccesService accesService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<File>>> getAllFiles(
            @RequestHeader(value = "X-User-Role") String role

    ){
        if (!role.equals("ADMIN")){
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can access this endpoint")
            );
        }
        else{
            List<File> files = fileService.getAllFiles()
    .stream()
//    .filter(file ->
//        file.getFileType() == FileType.PFE_REPORT ||
//        file.getFileType() == FileType.STAGE_REPORT ||
//        file.getFileType() == FileType.EDUCATIONAL_MATERIAL ||
//        file.getFileType() == FileType.SUPPLEMENTARY
//    )
    .toList();
            return ResponseEntity.ok(
                    ApiResponse.success(files,"All Files")
            );
        }
    }
    @Operation(summary = "Upload a file", description = "Upload a file to MinIO storage with optional cover page")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<tn.esprim.knowledgehub.resource.dto.response.ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional cover image. If absent, first PDF page is auto-extracted when file is published to library")
            @RequestParam(value = "coverPage", required = false) MultipartFile coverPage,
            @Parameter(description = "File type") @RequestParam("fileType") FileType fileType,
            @Parameter(description = "Bucket type (documents, pfa, pfe, stage)") @RequestParam("bucketType") String bucketType,
            @Parameter(description = "File title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "File description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Admins can set true to publish directly to library") @RequestParam(value = "isPublished", required = false) Boolean isPublished,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Name", required = false) String userEmail
    ) {
        try {
            log.info("Uploading file: {} by user: {}", fileType, userEmail);
            if (coverPage != null) {
                log.info("CoverPage provided in request. name={}, size={} bytes, contentType={}", coverPage.getOriginalFilename(), coverPage.getSize(), coverPage.getContentType());
            } else {
                log.info("No coverPage provided in request.");
            }

            // Parse userId, default to 0 if empty or null (for admin uploads)
            Long uploadedByUserId = 0L;
            if (userId != null && !userId.trim().isEmpty()) {
                try {
                    uploadedByUserId = Long.parseLong(userId.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId format: {}, using default 0", userId);
                }
            }

            // Use email or default
            String uploaderEmail = (userEmail != null && !userEmail.trim().isEmpty())
                    ? userEmail
                    : "admin@esprim.tn";
            boolean isTeacher = Objects.equals(role, "TEACHER");
            boolean isAdmin = Objects.equals(role, "ADMIN");
            if (!isTeacher && !isAdmin) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "Only admins and teachers can upload files")
                );
            }

            boolean published = isTeacher || (isAdmin && Boolean.TRUE.equals(isPublished));
            log.info("Upload flags -> isTeacher={}, isAdmin={}, isPublishedParam={}, computedPublished={}", isTeacher, isAdmin, isPublished, published);
            // Always handle cover during upload (provided or auto-extracted), regardless of publish status
            FileUploadResponse response = fileService.uploadFile(
                    file,
                    fileType,
                    uploadedByUserId,
                    uploaderEmail,
                    bucketType,
                    title,
                    description,
                    published,
                    coverPage,
                    true  // enableCoverForLibrary: ALWAYS true during upload
            );
            if (published){
                try{
                    aiandSearchService.indexFile(response.getFileId());

                }catch (Exception e){
                    log.warn("Failed to index file.", e);
                }
            }

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
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        try {
            boolean isAdmin = role.equals("ADMIN");

            Long userIdLong = null;
            if (userId != null && !userId.trim().isEmpty()) {
                try {
                    userIdLong = Long.parseLong(userId.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId format: {}", userId);
                }
            }

            File file = fileService.getFileById(fileId);

            if (!file.getIsPublished() && !isAdmin) {
                List<Long> usersWithAccess = accesService.fileAccess(fileId);
                log.debug("Users with access to file {}: {}", fileId, usersWithAccess);

                if (userIdLong == null || !usersWithAccess.contains(userIdLong)) {
                    return ResponseEntity.status(403).body(
                            ApiResponse.error("Forbidden", "This file is not yet published.")
                    );
                }
            }

            String presignedUrl = fileService.generatePresignedUrl(fileId, 30);
            log.info("Generated presigned URL for file {}: {}", fileId, presignedUrl);
            return ResponseEntity.ok(ApiResponse.success(presignedUrl, "Secure link generated"));

        } catch (Exception e) {
            log.error("Error generating view link for file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to generate link")
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
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        try {
            File file = fileService.getFileById(fileId);

            // Parse userId safely
            Long userIdLong = null;
            if (userId != null && !userId.trim().isEmpty()) {
                try {
                    userIdLong = Long.parseLong(userId.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId format: {}", userId);
                }
            }

            if (!role.equals("ADMIN") && (userIdLong == null || !file.getUploadedByUserId().equals(userIdLong))) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "Only admins and file owners can delete files")
                );
            }

            if (file.getIsPublished()) {
                try {
                    aiandSearchService.deleteFile(Math.toIntExact(fileId));
                } catch (Exception e) {
                    log.warn("Could not delete file from vector DB: {}", e.getMessage());
                }
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
            @PathVariable Long fileId,
            @RequestParam Boolean isPublished,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        try {
            boolean isAdmin = role.equals("ADMIN");
            boolean isTeacherAndOwner = role.equals("TEACHER")
                    && userId != null
                    && fileService.getFileById(fileId).getUploadedByUserId().equals(userId);

            if (!isAdmin && !isTeacherAndOwner) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "Only admins and file owners can update publication status")
                );
            }

            File file = fileService.updatePublicationStatus(fileId, isPublished, role);
            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(file, "Publication status updated successfully"));
        } catch (Exception e) {
            log.error("Error updating publication status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to update publication status")
            );
        }
    }

    @Operation(summary = "Attach or replace cover image for a file", description = "Upload a cover image and attach it to an existing file (Admin only)")
    @PostMapping(value = "/{fileId}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<tn.esprim.knowledgehub.resource.dto.response.ApiResponse<File>> attachCover(
            @PathVariable Long fileId,
            @RequestParam("coverPage") MultipartFile coverPage,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        try {
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(
                        tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error("Forbidden", "Only admins can attach covers")
                );
            }

            File updated = fileService.attachCoverToFile(fileId, coverPage);
            return ResponseEntity.ok(tn.esprim.knowledgehub.resource.dto.response.ApiResponse.success(updated, "Cover attached successfully"));
        } catch (Exception e) {
            log.error("Error attaching cover for file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    tn.esprim.knowledgehub.resource.dto.response.ApiResponse.error(e.getMessage(), "Failed to attach cover")
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
            @RequestHeader(value = "X-Auth-Id", required = false) String authId
    ) {
        try {
            File file = fileService.getFileById(fileId);
            boolean isStaff = role.equals("ADMIN") || role.equals("TEACHER");

            // Parse authId safely
            Long authIdLong = null;
            if (authId != null && !authId.trim().isEmpty()) {
                try {
                    authIdLong = Long.parseLong(authId.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid authId format: {}", authId);
                }
            }

            boolean isOwner = authIdLong != null && file.getUploadedByUserId().equals(authIdLong);

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


