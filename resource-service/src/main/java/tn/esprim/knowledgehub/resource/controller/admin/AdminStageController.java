package tn.esprim.knowledgehub.resource.controller.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.StageResponse;
import tn.esprim.knowledgehub.resource.enums.TypeStage;
import tn.esprim.knowledgehub.resource.service.StageService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stage")
@RequiredArgsConstructor
@Slf4j
public class AdminStageController {

    private final StageService stageService;


    @PostMapping("/authorize/student/{authId}")
    public ResponseEntity<ApiResponse<String>> authorize(
            @PathVariable Long authId,
            @RequestParam TypeStage typeStage,
            @RequestHeader(value = "X-User-Role") String role
    ){
        log.info("Admin authorizing student {} for stage type {}", authId, typeStage);

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can authorize students")
            );
        }

        try {
            stageService.authorizeStudent(authId, typeStage);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            null,
                            "Étudiant autorisé avec succès pour créer un stage de type " + typeStage
                    )
            );
        } catch (Exception e) {
            log.error("Error authorizing student {}: {}", authId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Erreur interne du serveur", e.getMessage())
            );
        }
    }


    @PostMapping("/authorize/class/{classId}")
    public ResponseEntity<ApiResponse<Integer>> authorizeClass(
            @PathVariable Long classId,
            @RequestParam TypeStage typeStage,
            @RequestHeader(value = "X-User-Role") String role
    ){
        log.info("Admin authorizing class {} for stage type {}", classId, typeStage);

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can authorize classes")
            );
        }

        try {
            int authorizedCount = stageService.authorizeClass(classId, typeStage);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            authorizedCount,
                            authorizedCount + " étudiant(s) autorisé(s) avec succès pour créer un stage de type " + typeStage
                    )
            );
        } catch (Exception e) {
            log.error("Error authorizing class {}: {}", classId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Erreur interne du serveur", e.getMessage())
            );
        }
    }

    /**
     * Récupérer tous les stages (pour l'administrateur)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StageResponse>>> getAllStages(
            @RequestHeader(value = "X-User-Role") String role
    ) {
        log.info("Admin requesting all stages");

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can view all stages")
            );
        }

        try {
            List<StageResponse> stages = stageService.getAllStages();
            return ResponseEntity.ok(
                    ApiResponse.success(
                            stages,
                            stages.size() + " stage(s) trouvé(s)"
                    )
            );
        } catch (Exception e) {
            log.error("Error retrieving all stages: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Erreur interne du serveur", e.getMessage())
            );
        }
    }

    /**
     * Assigner un superviseur (enseignant) à un stage
     */
    @PostMapping("/{stageId}/assign-supervisor/{authId}")
    public ResponseEntity<ApiResponse<StageResponse>> assignSupervisor(
            @PathVariable Long stageId,
            @PathVariable Long authId,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        log.info("Admin assigning supervisor {} to stage {}", authId, stageId);

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can assign supervisors")
            );
        }

        try {
            StageResponse updatedStage = stageService.assignSupervisor(stageId, authId);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            updatedStage,
                            "Superviseur assigné avec succès"
                    )
            );
        } catch (Exception e) {
            log.error("Error assigning supervisor to stage {}: {}", stageId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Erreur interne du serveur", e.getMessage())
            );
        }
    }

    @DeleteMapping("/remove-all-authorized-students")
    public ResponseEntity<ApiResponse<StageResponse>> removeAllAuthorizedStudents(
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can remove authorized students")
            );
        }
        stageService.deleteAllDemande();
        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "toute autorisation a été supprimée"
                )
        );

    }

        @PatchMapping("/{stageId}/close")
        public ResponseEntity<ApiResponse<StageResponse>> closeStage(
                        @PathVariable Long stageId,
                        @RequestHeader(value = "X-User-Role") String role
        ) {
                log.info("Admin closing stage {}", stageId);

                if (!role.equals("ADMIN")) {
                        return ResponseEntity.status(403).body(
                                        ApiResponse.error("Forbidden", "Only admins can close stages")
                        );
                }

                try {
                        StageResponse updatedStage = stageService.closeStage(stageId);
                        return ResponseEntity.ok(
                                        ApiResponse.success(updatedStage, "Stage clôturé avec succès")
                        );
                } catch (Exception e) {
                        log.error("Error closing stage {}: {}", stageId, e.getMessage(), e);
                        return ResponseEntity.status(500).body(
                                        ApiResponse.error("Erreur interne du serveur", e.getMessage())
                        );
                }
        }

        @PatchMapping("/{stageId}/open")
        public ResponseEntity<ApiResponse<StageResponse>> openStage(
                        @PathVariable Long stageId,
                        @RequestHeader(value = "X-User-Role") String role
        ) {
                log.info("Admin opening stage {}", stageId);

                if (!role.equals("ADMIN")) {
                        return ResponseEntity.status(403).body(
                                        ApiResponse.error("Forbidden", "Only admins can open stages")
                        );
                }

                try {
                        StageResponse updatedStage = stageService.openStage(stageId);
                        return ResponseEntity.ok(
                                        ApiResponse.success(updatedStage, "Stage rouvert avec succès")
                        );
                } catch (Exception e) {
                        log.error("Error opening stage {}: {}", stageId, e.getMessage(), e);
                        return ResponseEntity.status(500).body(
                                        ApiResponse.error("Erreur interne du serveur", e.getMessage())
                        );
                }
        }

        @PatchMapping("/{stageId}/can-edit")
        public ResponseEntity<ApiResponse<StageResponse>> updateCanEdit(
                        @PathVariable Long stageId,
                        @RequestParam boolean canEdit,
                        @RequestHeader(value = "X-User-Role") String role
        ) {
                log.info("Admin updating canEdit={} for stage {}", canEdit, stageId);

                if (!role.equals("ADMIN")) {
                        return ResponseEntity.status(403).body(
                                        ApiResponse.error("Forbidden", "Only admins can update canEdit")
                        );
                }

                try {
                        StageResponse updatedStage = stageService.updateCanEdit(stageId, canEdit);
                        return ResponseEntity.ok(
                                        ApiResponse.success(updatedStage, "canEdit mis à jour avec succès")
                        );
                } catch (Exception e) {
                        log.error("Error updating canEdit for stage {}: {}", stageId, e.getMessage(), e);
                        return ResponseEntity.status(500).body(
                                        ApiResponse.error("Erreur interne du serveur", e.getMessage())
                        );
                }
        }

        @DeleteMapping("/{stageId}")
        public ResponseEntity<ApiResponse<Void>> deleteStage(
                        @PathVariable Long stageId,
                        @RequestHeader(value = "X-User-Role") String role
        ) {
                log.info("Admin deleting stage {}", stageId);

                if (!role.equals("ADMIN")) {
                        return ResponseEntity.status(403).body(
                                        ApiResponse.error("Forbidden", "Only admins can delete stages")
                        );
                }

                try {
                        stageService.deleteStage(stageId);
                        return ResponseEntity.ok(
                                        ApiResponse.success(null, "Stage supprimé avec succès")
                        );
                } catch (Exception e) {
                        log.error("Error deleting stage {}: {}", stageId, e.getMessage(), e);
                        return ResponseEntity.status(500).body(
                                        ApiResponse.error("Erreur interne du serveur", e.getMessage())
                        );
                }
        }
}
