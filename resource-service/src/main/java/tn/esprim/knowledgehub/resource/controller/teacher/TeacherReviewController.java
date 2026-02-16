package tn.esprim.knowledgehub.resource.controller.teacher;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.dto.request.UpdatePFEStatusRequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.service.PFAService;
import tn.esprim.knowledgehub.resource.service.PFEService;
import tn.esprim.knowledgehub.resource.service.StageService;

@RestController
@RequestMapping("/api/teacher/review")
@RequiredArgsConstructor
public class TeacherReviewController {

    private final PFAService pfaService;
    private final PFEService pfeService;
    private final StageService stageService;


    @PutMapping("/pfe/{pfeId}/status")
    public ResponseEntity<ApiResponse<PFE>> updatePFEStatus(
            @PathVariable Long pfeId,
            @Valid @RequestBody UpdatePFEStatusRequest request,
            @RequestHeader("X-User-Id") Long teacherId
    ) {
        PFE pfe = pfeService.getPFEById(pfeId);
        if (pfe == null) {
            return ResponseEntity.notFound().build();
        }
        if (!pfe.getSupervisorId().equals(teacherId)) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "You are not allowed to update this PFE")
            );
        }
        try {
            PFE updatedPFE = pfeService.updatePFEStatus(pfe, request, teacherId);
            return ResponseEntity.ok(
                    ApiResponse.success(updatedPFE, "PFE status updated successfully")
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to update PFE status")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Internal server error", "Failed to update PFE status")
            );
        }
    }
}
