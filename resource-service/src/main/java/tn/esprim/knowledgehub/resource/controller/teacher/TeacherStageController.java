package tn.esprim.knowledgehub.resource.controller.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.StageResponse;
import tn.esprim.knowledgehub.resource.service.StageService;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/stage")
@RequiredArgsConstructor
@Slf4j
public class TeacherStageController {

    private final StageService stageService;

    /**
     * Récupérer tous les stages supervisés par l'enseignant connecté
     */
    @GetMapping("/supervised")
    public ResponseEntity<ApiResponse<List<StageResponse>>> getSupervisedStages(
            @RequestHeader(value = "X-User-Id") Long teacherId,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        log.info("Teacher {} requesting supervised stages", teacherId);

        if (!role.equals("TEACHER")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only teachers can access this endpoint")
            );
        }

        try {
            List<StageResponse> stages = stageService.getStagesBySupervisor(teacherId);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            stages,
                            stages.size() + " stage(s) supervisé(s) trouvé(s)"
                    )
            );
        } catch (Exception e) {
            log.error("Error retrieving supervised stages for teacher {}: {}", teacherId, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Erreur interne du serveur", e.getMessage())
            );
        }
    }
}
