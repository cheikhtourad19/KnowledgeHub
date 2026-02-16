package tn.esprim.knowledgehub.resource.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.dto.request.UpdateDemandStatusRequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.entity.DemandePFE;
import tn.esprim.knowledgehub.resource.enums.DemandStatus;
import tn.esprim.knowledgehub.resource.service.PFEService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pfe")
@RequiredArgsConstructor
public class AdminPFEController {

    private final PFEService pfeService;


    @GetMapping("/demandes")
    public ResponseEntity<ApiResponse<List<DemandePFE>>> getAllDemandes(
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can view all demands")
            );
        }

        List<DemandePFE> demandes = pfeService.getAllDemandePFE();
        return ResponseEntity.ok(
                ApiResponse.success(demandes, "All PFE demands retrieved successfully")
        );
    }


    @GetMapping("/demande/{id}")
    public ResponseEntity<ApiResponse<DemandePFE>> getDemandePFEById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can view demands")
            );
        }

        try {
            DemandePFE demande = pfeService.getDemandePFEById(id);
            return ResponseEntity.ok(
                    ApiResponse.success(demande, "PFE demand retrieved successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", e.getMessage())
            );
        }
    }


    @PutMapping("/demande/{id}/status")
    public ResponseEntity<ApiResponse<DemandePFE>> updateDemandStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateDemandStatusRequest request,
            @RequestHeader(value = "X-User-Id") Long adminId,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can update demand status")
            );
        }

        try {
            DemandePFE updated = pfeService.updateDemandStatus(id, request, adminId);
            return ResponseEntity.ok(
                    ApiResponse.success(updated, "Demand status updated successfully to " + request.getStatus())
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("Bad Request", e.getMessage())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", e.getMessage())
            );
        }
    }


    @GetMapping("/demandes/status/{status}")
    public ResponseEntity<ApiResponse<List<DemandePFE>>> getDemandesByStatus(
            @PathVariable DemandStatus status,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can filter demands")
            );
        }

        List<DemandePFE> demandes = pfeService.getDemandePFEByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success(demandes, "Demands with status " + status + " retrieved successfully")
        );
    }
}
