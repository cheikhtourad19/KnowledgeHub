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
            @RequestParam(value = "teacherId") Long teacherId,
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
            if (request.getStatus().equals(DemandStatus.ACCEPTED)){
                TeacherDTO teacherDTO = userServiceClient.getTeacherById(teacherId).getData();
                PFE pfe = PFE.builder()
                        .date_debut(updated.getDateDebut())
                        .date_fin(updated.getDateFin())
                        .studentId(updated.getStudentId())
                        .studentEmail(updated.getStudentEmail())
                        .studentName(updated.getStudentName())
                        .entreprise_name(updated.getEntrepriseName())
                        .title(updated.getPfeTitle())
                        .year(Year.now().getValue())
                        .supervisorId(teacherId)
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
    public ResponseEntity<ApiResponse> assignJury(

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
        if (request.getJuryId() !=null){
            TeacherDTO chef_jury = userServiceClient.getTeacherById(request.getJuryId()).getData();
            pfe.setChefDeJuryId(chef_jury.getTeacherId());
            pfe.setChefDeJuryName(chef_jury.getNom()+" "+chef_jury.getPrenom());
        }

        if (request.getReporteur_id()!=null){
            TeacherDTO reporteur = userServiceClient.getTeacherById(request.getReporteur_id()).getData();
            pfe.setRapporteurId(reporteur.getTeacherId());
            pfe.setRapporteurName(reporteur.getNom()+" "+reporteur.getPrenom());
        }



        pfeService.savePFE(pfe);
        return ResponseEntity.ok(ApiResponse.success(null,"Jury assigned successfully"));



    }
}
