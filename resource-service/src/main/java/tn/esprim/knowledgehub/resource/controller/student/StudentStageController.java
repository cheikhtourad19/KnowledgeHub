package tn.esprim.knowledgehub.resource.controller.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;
import tn.esprim.knowledgehub.resource.dto.request.StudentCreateStageRequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.dto.response.StageResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.Stage;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.service.FileService;
import tn.esprim.knowledgehub.resource.service.StageService;
import tn.esprim.knowledgehub.resource.repository.StageRepository;

@RestController
@RequestMapping("/api/student/stage")
@RequiredArgsConstructor
@Slf4j
public class StudentStageController {

    private final StageService stageService;
    private final FileService fileService;
    private final UserServiceClient userServiceClient;
    private final StageRepository stageRepository;


    @PostMapping
    public ResponseEntity<ApiResponse<StageResponse>> createStage(
            @Valid @RequestBody StudentCreateStageRequest request,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("Student {} attempting to create a stage", userId);

        try {
            StageResponse stageResponse = stageService.createStageForStudent(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            stageResponse,
                            "Stage créé avec succès. En attente de l'assignation d'un superviseur par l'admin."
                    ));
        } catch (RuntimeException e) {
            log.error("Error creating stage for student {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            "Erreur lors de la création du stage",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error creating stage for student {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Erreur interne du serveur",
                            "Une erreur inattendue s'est produite"
                    ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StageResponse>> getStageById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        Stage stage = stageService.getStageById(id);

        if (stage == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Not Found", "Stage not found with id: " + id));
        }

        boolean isAdmin = role.equals("ADMIN");
        boolean isSupervisor = false;
        if (stage.getSupervisorId() != null) {
            isSupervisor = stage.getSupervisorId().equals(userId);
        }
        boolean isOwner = stage.getStudentId().equals(userId);

        if (isAdmin || isOwner || isSupervisor) {
            // Convert entity to DTO to avoid Hibernate lazy loading serialization issues
            StageResponse stageResponse = stageService.mapToResponse(stage);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            stageResponse,
                            "Stage retrieved successfully"
                    )
            );
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "You are not allowed to access this stage")
            );
        }
    }

    @GetMapping("/my-stage")
    public ResponseEntity<ApiResponse<Object>> getMyStage(
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("Student {} requesting their stages", userId);
        try {
            var stages = stageService.getStagesByStudentId(userId);
            return ResponseEntity.ok(
                    ApiResponse.success(stages, "Stages retrieved successfully")
            );
        } catch (Exception e) {
            log.error("Error retrieving stages for student {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Erreur interne du serveur",
                            "Une erreur inattendue s'est produite"
                    ));
        }
    }

    @GetMapping("/check-authorization")
    public ResponseEntity<ApiResponse<Object>> checkAuthorization(
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("Student {} checking stage authorization", userId);
        try {
            var authorization = stageService.getStudentAuthorization(userId);
            if (authorization != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(authorization, "Authorization found")
                );
            } else {
                return ResponseEntity.ok(
                        ApiResponse.success(null, "No authorization found")
                );
            }
        } catch (Exception e) {
            log.error("Error checking authorization for student {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Erreur interne du serveur",
                            "Une erreur inattendue s'est produite"
                    ));
        }
    }

    @PostMapping("/upload-report/{stageId}")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadReportFile(
            @PathVariable Long stageId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("Student {} attempting to upload report for stage {}", userId, stageId);

        try {
            Stage stage = stageService.getStageById(stageId);
            if (stage == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Stage non trouvé avec l'ID: " + stageId)
                );
            }

            if (!stage.getStudentId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Vous n'êtes pas autorisé à uploader des fichiers pour ce stage")
                );
            }

            // Check if the stage is from a past year (not editable)
            int currentYear = java.time.LocalDate.now().getYear();
            if (stage.getYear() != null && stage.getYear() < currentYear) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Ce stage est de l'année " + stage.getYear() + " et n'est plus modifiable")
                );
            }

            StudentDTO student = userServiceClient.getStudentById(stage.getStudentId()).getData();
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Étudiant non trouvé")
                );
            }

            FileUploadResponse response = fileService.uploadFile(
                    file,
                    FileType.STAGE_REPORT,
                    userId,
                    student.getEmail(),
                    "stage",
                    title != null ? title : "Rapport de stage - " + stage.getTitle(),
                    description,
                    false
            );

            File uploadedFile = fileService.getFileById(response.getFileId());
            stage.setReportFile(uploadedFile);
            stageRepository.save(stage);

            log.info("Report uploaded successfully for stage {}", stageId);
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Rapport uploadé avec succès")
            );
        } catch (Exception e) {
            log.error("Error uploading report for stage {}: {}", stageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Erreur interne", "Erreur lors de l'upload du rapport: " + e.getMessage())
            );
        }
    }

    @PostMapping("/upload-journal/{stageId}")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadJournalFile(
            @PathVariable Long stageId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("Student {} attempting to upload journal for stage {}", userId, stageId);

        try {
            Stage stage = stageService.getStageById(stageId);
            if (stage == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Stage non trouvé avec l'ID: " + stageId)
                );
            }

            if (!stage.getStudentId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Vous n'êtes pas autorisé à uploader des fichiers pour ce stage")
                );
            }

            // Check if the stage is from a past year (not editable)
            int currentYear = java.time.LocalDate.now().getYear();
            if (stage.getYear() != null && stage.getYear() < currentYear) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Ce stage est de l'année " + stage.getYear() + " et n'est plus modifiable")
                );
            }

            StudentDTO student = userServiceClient.getStudentById(stage.getStudentId()).getData();
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Étudiant non trouvé")
                );
            }

            FileUploadResponse response = fileService.uploadFile(
                    file,
                    FileType.JOURNAL,
                    userId,
                    student.getEmail(),
                    "journal",
                    title != null ? title : "Journal de stage - " + stage.getTitle(),
                    description,
                    false
            );

            File uploadedFile = fileService.getFileById(response.getFileId());
            stage.setJournalFile(uploadedFile);
            stageRepository.save(stage);

            log.info("Journal uploaded successfully for stage {}", stageId);
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Journal uploadé avec succès")
            );
        } catch (Exception e) {
            log.error("Error uploading journal for stage {}: {}", stageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Erreur interne", "Erreur lors de l'upload du journal: " + e.getMessage())
            );
        }
    }

    @PostMapping("/upload-attestation/{stageId}")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadAttestationFile(
            @PathVariable Long stageId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("Student {} attempting to upload attestation for stage {}", userId, stageId);

        try {
            Stage stage = stageService.getStageById(stageId);
            if (stage == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Stage non trouvé avec l'ID: " + stageId)
                );
            }

            if (!stage.getStudentId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Vous n'êtes pas autorisé à uploader des fichiers pour ce stage")
                );
            }

            // Check if the stage is from a past year (not editable)
            int currentYear = java.time.LocalDate.now().getYear();
            if (stage.getYear() != null && stage.getYear() < currentYear) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Ce stage est de l'année " + stage.getYear() + " et n'est plus modifiable")
                );
            }

            StudentDTO student = userServiceClient.getStudentById(stage.getStudentId()).getData();
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Étudiant non trouvé")
                );
            }

            FileUploadResponse response = fileService.uploadFile(
                    file,
                    FileType.ATTESTATION,
                    userId,
                    student.getEmail(),
                    "attestation",
                    title != null ? title : "Attestation de stage - " + stage.getTitle(),
                    description,
                    false
            );

            File uploadedFile = fileService.getFileById(response.getFileId());
            stage.setAttestationFile(uploadedFile);
            stageRepository.save(stage);

            log.info("Attestation uploaded successfully for stage {}", stageId);
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Attestation uploadée avec succès")
            );
        } catch (Exception e) {
            log.error("Error uploading attestation for stage {}: {}", stageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Erreur interne", "Erreur lors de l'upload de l'attestation: " + e.getMessage())
            );
        }
    }
}
