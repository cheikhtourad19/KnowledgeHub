package tn.esprim.knowledgehub.resource.controller.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.dto.request.StudentCreateStageRequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.StageResponse;
import tn.esprim.knowledgehub.resource.entity.Stage;
import tn.esprim.knowledgehub.resource.service.StageService;

@RestController
@RequestMapping("/api/student/stage")
@RequiredArgsConstructor
@Slf4j
public class StudentStageController {

    private final StageService stageService;


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
}

