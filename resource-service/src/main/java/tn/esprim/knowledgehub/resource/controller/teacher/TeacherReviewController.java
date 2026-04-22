package tn.esprim.knowledgehub.resource.controller.teacher;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.dto.request.UpdatePFEStatusRequest;
import tn.esprim.knowledgehub.resource.dto.request.RapporteurFeedbackRequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.service.FileService;
import tn.esprim.knowledgehub.resource.service.PFAService;
import tn.esprim.knowledgehub.resource.service.PFEService;
import tn.esprim.knowledgehub.resource.service.StageService;

@RestController
@RequestMapping("/api/teacher/pfe")
@RequiredArgsConstructor
public class TeacherReviewController {

    private final PFEService pfeService;
    private final FileService fileService;


    @GetMapping("/supervised")
    public ResponseEntity<ApiResponse> getTeacherPFE(
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        if (!role.equals("TEACHER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only teachers and admins can access this endpoint")
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(pfeService.getPFEBySupervisor(userId), "All PFE")
        );
    }

    @GetMapping("/jury")
    public ResponseEntity<ApiResponse> getTeacherJury(
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") Long userId
    ){
        if (!role.equals("TEACHER") && !role.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only teachers and admins can access this endpoint")
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(pfeService.getPFEByJury(userId), "All PFE")
        );


    }

    @GetMapping("/repporteur")
    public ResponseEntity<ApiResponse> getTeacherRepporteur(
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") Long userId
    ){
        if (!role.equals("TEACHER") && !role.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only teachers and admins can access this endpoint")
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(pfeService.getPFEByRapporteur(userId), "All PFE")
        );


    }


    @PutMapping("/pfe/{pfeId}/status")
    public ResponseEntity<ApiResponse<PFE>> updatePFEStatus(
            @PathVariable Long pfeId,
            @Valid @RequestBody UpdatePFEStatusRequest request,
            @RequestHeader("X-User-Id") Long teacherId
    ) {
        PFE pfe = pfeService.getPFEById(pfeId);
        if (pfe == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error("Not Found", "PFE not found with id: " + pfeId)
            );
        }
        if (!pfe.getSupervisorId().equals(teacherId)) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "You are not allowed to update this PFE")
            );
        }
        try {
            PFE updatedPFE = pfeService.updatePFEStatus(pfe, request, teacherId);

            if (request.getStatus() == SubmissionStatus.REJECTED && updatedPFE.getReportFile() != null) {
                Long fileId = updatedPFE.getReportFile().getId();
                updatedPFE.setReportFile(null);
                pfeService.savePFE(updatedPFE);
                fileService.deleteFile(fileId);
            }
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

    @PutMapping("/pfe/{pfeId}/rapporteur-feedback")
    public ResponseEntity<ApiResponse<PFE>> addRapporteurFeedback(
            @PathVariable Long pfeId,
            @Valid @RequestBody RapporteurFeedbackRequest request,
            @RequestHeader("X-User-Id") Long rapporteurId
    ) {
        PFE pfe = pfeService.getPFEById(pfeId);
        if (pfe == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error("Not Found", "PFE not found with id: " + pfeId)
            );
        }

        // Check if the user is the rapporteur
        if (!pfe.getRapporteurId().equals(rapporteurId)) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "You are not the rapporteur for this PFE")
            );
        }

        try {
            PFE updatedPFE = pfeService.addRapporteurFeedback(pfe, request.getFeedback());
            return ResponseEntity.ok(
                    ApiResponse.success(updatedPFE, "Rapporteur feedback added successfully")
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "Failed to add rapporteur feedback")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Internal server error", "Failed to add rapporteur feedback")
            );
        }
    }


}
