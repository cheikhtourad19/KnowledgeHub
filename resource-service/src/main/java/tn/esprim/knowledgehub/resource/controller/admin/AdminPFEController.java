package tn.esprim.knowledgehub.resource.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.TeacherDTO;
import tn.esprim.knowledgehub.resource.dto.request.AssignJury;
import tn.esprim.knowledgehub.resource.dto.request.UpdateDemandStatusRequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.entity.DemandePFE;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.enums.DemandStatus;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.service.FileService;
import tn.esprim.knowledgehub.resource.service.PFEService;

import java.time.Year;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/pfe")
@RequiredArgsConstructor
public class AdminPFEController {

    private final PFEService pfeService;
    private final UserServiceClient userServiceClient;
    private final FileService fileService;


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

    @DeleteMapping("/demandes")
    public ResponseEntity<ApiResponse<Void>> deleteAllDemandes(
            @RequestHeader(value = "X-User-Role") String role

    ){
        if (!role.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can delete all demands")
            );
        }
        pfeService.removeAllDemandes();
        return ResponseEntity.ok(ApiResponse.success(null,"All PFE demands deleted successfully"));
    }
    @DeleteMapping("/demande/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDemande(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role") String role
    ){
        if (!role.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can delete demands")
            );
        }
        try {
            pfeService.removeDemande(id);
            log.info("PFE demand with id {} deleted successfully", id);
        }catch (Exception e){
            log.error("Error deleting demand with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Error check the logs", e.getMessage())
            );
        }

        return ResponseEntity.ok(ApiResponse.success(null,"PFE demand deleted successfully"));

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
            @RequestParam(value = "teacherAuthId", required = false) Long teacherAuthId,
            @RequestBody @Valid UpdateDemandStatusRequest request,
            @RequestHeader(value = "X-Auth-Id") Long adminAuthId,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can update demand status")
            );
        }

        try {
            DemandePFE updated = pfeService.updateDemandStatus(id, request, adminAuthId);
            if (request.getStatus().equals(DemandStatus.ACCEPTED)){
                if (teacherAuthId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            ApiResponse.error("Bad Request", "teacherAuthId is required when accepting a demand")
                    );
                }
                TeacherDTO teacherDTO = userServiceClient.getTeacherByAuthId(String.valueOf(teacherAuthId)).getData();
                PFE pfe = PFE.builder()
                        .date_debut(updated.getDateDebut())
                        .date_fin(updated.getDateFin())
                        .studentAuthId(updated.getStudentAuthId())
                        .studentEmail(updated.getStudentEmail())
                        .studentName(updated.getStudentName())
                        .entreprise_name(updated.getEntrepriseName())
                        .title(updated.getPfeTitle())
                        .year(Year.now().getValue())
                        .supervisorAuthId(teacherAuthId)
                        .supervisorName(teacherDTO.getNom())
                        .status(SubmissionStatus.CREATED)
                        .createdDate(new Date())

                        .build();

                pfeService.savePFE(pfe);
            }


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


    @PostMapping("/assign-jury/{id}")
    public ResponseEntity<ApiResponse<?>> assignJury(

            @RequestBody AssignJury request, @RequestHeader(value = "X-User-Role") String role
            ,@PathVariable Long id
    ){

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can assign jury")
            );
        }
        PFE pfe = pfeService.getPFEById(id);

        log.info("request : {}",request);
        if (request.getJuryAuthId() !=null){
            TeacherDTO chef_jury = userServiceClient.getTeacherByAuthId(String.valueOf(request.getJuryAuthId())).getData();
            pfe.setChefDeJuryAuthId(chef_jury.getAuthId() != null ? Long.valueOf(chef_jury.getAuthId()) : request.getJuryAuthId());
            pfe.setChefDeJuryName(chef_jury.getNom()+" "+chef_jury.getPrenom());
        }

        if (request.getReporteurAuthId()!=null){
            TeacherDTO reporteur = userServiceClient.getTeacherByAuthId(String.valueOf(request.getReporteurAuthId())).getData();
            pfe.setRapporteurAuthId(reporteur.getAuthId() != null ? Long.valueOf(reporteur.getAuthId()) : request.getReporteurAuthId());
            pfe.setRapporteurName(reporteur.getNom()+" "+reporteur.getPrenom());
        }



        pfeService.savePFE(pfe);
        return ResponseEntity.ok(ApiResponse.success(null,"Jury assigned successfully"));



    }

    @PutMapping("/pfe/{id}/cloture")
    public ResponseEntity<ApiResponse<?>> closePFE(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can close PFE")
            );
        }

        try {
            PFE pfe = pfeService.getPFEById(id);
            if (pfe == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "PFE not found with id: " + id)
                );
            }

            PFE closedPFE = pfeService.closePFE(pfe);
            return ResponseEntity.ok(
                    ApiResponse.success(closedPFE, "PFE closed successfully")
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Bad Request", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Internal server error", "Failed to close PFE")
            );
        }
    }

    @PutMapping("/pfe/{id}/cancel-cloture")
    public ResponseEntity<ApiResponse<?>> cancelCloturePFE(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can cancel PFE closure")
            );
        }

        try {
            PFE pfe = pfeService.getPFEById(id);
            if (pfe == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "PFE not found with id: " + id)
                );
            }

            PFE reopenedPFE = pfeService.cancelPFECloture(pfe);
            return ResponseEntity.ok(
                    ApiResponse.success(reopenedPFE, "PFE closure canceled successfully")
            );
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Bad Request", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Internal server error", "Failed to cancel PFE closure")
            );
        }
    }

    @DeleteMapping("/pfe/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePFE(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equalsIgnoreCase(role))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Forbidden", "Only admins can delete PFE"));

        pfeService.deletePFEWithDemande(id);
        return ResponseEntity.ok(ApiResponse.success(null, "PFE deleted successfully"));
    }


}

