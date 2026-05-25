package tn.esprim.knowledgehub.resource.controller.student;

import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;
import tn.esprim.knowledgehub.resource.client.dto.TeacherDTO;
import tn.esprim.knowledgehub.resource.dto.request.CreateDemandePFEFormRequest;
import tn.esprim.knowledgehub.resource.dto.request.CreateDemandePFERequest;
import tn.esprim.knowledgehub.resource.dto.request.CreatePFERequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.DemandePFE;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.service.FileService;
import tn.esprim.knowledgehub.resource.service.PFEService;

import java.util.List;

@RestController
@RequestMapping("/api/student/pfe")
@RequiredArgsConstructor
public class StudentPFEController {

    private final PFEService pfeService;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;
    private final FileService fileService;


    @PostMapping(value = "/demande", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DemandePFE>> createDemandePFE(
            @ModelAttribute @Valid CreateDemandePFEFormRequest formRequest,
            @RequestHeader(value = "X-Auth-Id") Long authId,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        try {

            if (!role.equals("STUDENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Only students can create PFE demands")
                );
            }

            DemandePFE demandes = pfeService.getDemandePFEByStudent(authId);
            if (demandes!=null){
                pfeService.removeDemande(demandes.getId());
            }
            if (formRequest.getFile() == null || formRequest.getFile().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ApiResponse.error("Bad Request", "File is required")
                );
            }


            CreateDemandePFERequest request = formRequest.toServiceRequest();


            DemandePFE demande = pfeService.createDemandePFE(request, formRequest.getFile(), authId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(demande, "PFE demand created successfully")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Internal Server Error", "Error creating PFE demand: " + e.getMessage())
            );
        }
    }


    @GetMapping("/demandes")
    public ResponseEntity<ApiResponse<DemandePFE>> getMyDemandes(
            @RequestHeader(value = "X-Auth-Id") Long authId,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("STUDENT")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only students can view their demands")
            );
        }

        DemandePFE demandes = pfeService.getDemandePFEByStudent(authId);
        return ResponseEntity.ok(
                ApiResponse.success(demandes, "PFE demands retrieved successfully")
        );
    }


    @GetMapping("/demande/{id}")
    public ResponseEntity<ApiResponse<DemandePFE>> getDemandePFEById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Id") Long authId,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        try {
            DemandePFE demande = pfeService.getDemandePFEById(id);


            if (role.equals("STUDENT") && !demande.getStudentAuthId().equals(authId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "You are not allowed to access this demand")
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.success(demande, "PFE demand retrieved successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", e.getMessage())
            );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPFE(
            @RequestBody @Valid CreatePFERequest request,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can create PFE")
            );
        }

        StudentDTO student = userServiceClient.getStudentByAuthId(String.valueOf(request.getStudentAuthId())).getData();
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", "Student not found with authId: " + request.getStudentAuthId())
            );
        }

        TeacherDTO supervisor = userServiceClient.getTeacherByAuthId(String.valueOf(request.getSupervisorAuthId())).getData();
        if (supervisor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", "Supervisor not found with authId: " + request.getSupervisorAuthId())
            );
        }

        PFE pfe = PFE.builder()
                .studentAuthId(Long.valueOf(student.getAuthId()))
                .title(request.getTitle())
                .year(request.getYear())
                .status(SubmissionStatus.UNDER_REVIEW)
                .build();

        pfe.setSupervisorAuthId(Long.valueOf(supervisor.getAuthId()));
        pfe.setSupervisorName(supervisor.getNom() + " " + supervisor.getPrenom());
        pfe.setStudentEmail(student.getEmail());
        pfe.setStudentName(student.getNom() + " " + student.getPrenom());

        if (request.getChefDeJuryAuthId() != null) {
            TeacherDTO chef = userServiceClient.getTeacherByAuthId(String.valueOf(request.getChefDeJuryAuthId())).getData();
            if (chef == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Chef de Jury not found with authId: " + request.getChefDeJuryAuthId())
                );
            }
            pfe.setChefDeJuryAuthId(Long.valueOf(chef.getAuthId()));
            pfe.setChefDeJuryName(chef.getNom() + " " + chef.getPrenom());
        }

        if (request.getRapporteurAuthId() != null) {
            TeacherDTO rapporteur = userServiceClient.getTeacherByAuthId(String.valueOf(request.getRapporteurAuthId())).getData();
            if (rapporteur == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("Not Found", "Rapporteur not found with authId: " + request.getRapporteurAuthId())
                );
            }
            pfe.setRapporteurAuthId(Long.valueOf(rapporteur.getAuthId()));
            pfe.setRapporteurName(rapporteur.getNom() + " " + rapporteur.getPrenom());
        }
        pfe.setDate_debut(request.getDateDebut());
        pfe.setDate_fin(request.getDateFin());
        pfe.setEntreprise_name(request.getEntrepriseName());

        PFE saved = pfeService.savePFE(pfe);

        notificationClient.sendNotificationAsync(
                NotificationRequest.builder()
                        .type(NotificationType.EMAIL)
                        .recipientEmail(student.getEmail())
                        .subject("Vous avez été assigné un PFE")
                        .content("Vous avez été assigné un PFE, veuillez consulter votre espace pour plus de détails. C'est dans cet espace que vous aurez à déposer vos documents demandés.")
                        .userId(student.getAuthId())
                        .build()
        );

        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("PFE created successfully")
                        .data(saved)
                        .build()
        );
    }

    @PostMapping("/upload-report/{idPFE}")
    public ResponseEntity<ApiResponse> uploadReportFile(
            @PathVariable Long idPFE,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Auth-Id") Long authId
    ) {
        PFE pfe = pfeService.getPFEById(idPFE);
        if (pfe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", "PFE not found with ID: " + idPFE)
            );
        }


        if (!pfe.getStudentAuthId().equals(authId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "You are not allowed to upload files for this PFE")
            );
        }

        // Check if upload is allowed based on new workflow
        if (Boolean.TRUE.equals(pfe.getCloture())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "This PFE is closed, you cannot upload files")
            );
        }

        if (pfe.getStatus().equals(SubmissionStatus.UNDER_REVIEW)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "This PFE is under review, you cannot upload files")
            );
        }

        if (pfe.getStatus().equals(SubmissionStatus.APPROVED)) {
            // Allow upload only if rapporteur feedback exists
            if (pfe.getReporteurFeedback() == null || pfe.getReporteurFeedback().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Rapporteur feedback is required before uploading revised report")
                );
            }
        }



        StudentDTO student = userServiceClient.getStudentByAuthId(String.valueOf(pfe.getStudentAuthId())).getData();
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", "Student not found")
            );
        }

        FileUploadResponse response = fileService.uploadFile(
                file,
                FileType.PFE_REPORT,
                authId,
                student.getEmail(),
                "pfe",
                title,
                description,
                false
        );

        File uploadedFile = fileService.getFileById(response.getFileId());
                Long oldReportFileId = pfe.getReportFile() != null ? pfe.getReportFile().getId() : null;
                pfe.setReportFile(uploadedFile);
                pfe.setStatus(SubmissionStatus.SUBMITTED);
                pfeService.savePFE(pfe);
                if (oldReportFileId != null) {
                        fileService.deleteFile(oldReportFileId);
        }


        if (pfe.getSupervisorAuthId() != null) {
            TeacherDTO teacherDTO = userServiceClient.getTeacherByAuthId(String.valueOf(pfe.getSupervisorAuthId())).getData();
            if (teacherDTO != null) {
                notificationClient.sendNotificationAsync(
                        NotificationRequest.builder()
                                .type(NotificationType.EMAIL)
                                .recipientEmail(teacherDTO.getEmail())
                                .content("En attente de validation du rapport de " + student.getNom() + " " + student.getPrenom())
                                .subject("Dépôt de rapport PFE")
                                .build()
                );
            }
        }

        return ResponseEntity.ok(
                ApiResponse.success(response, "File uploaded successfully")
        );
    }


    @PostMapping("/upload-attestation/{idPFE}")
    public ResponseEntity<ApiResponse> uploadAttestationFile(
            @PathVariable Long idPFE,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Auth-Id") Long authId
    ) {
        PFE pfe = pfeService.getPFEById(idPFE);
        if (pfe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", "PFE not found with ID: " + idPFE)
            );
        }


        if (!pfe.getStudentAuthId().equals(authId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "You are not allowed to upload files for this PFE")
            );
        }

        // Check if upload is allowed based on new workflow
        if (Boolean.TRUE.equals(pfe.getCloture())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "This PFE is closed, you cannot upload files")
            );
        }

        if (pfe.getStatus().equals(SubmissionStatus.UNDER_REVIEW)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "This PFE is under review, you cannot upload files")
            );
        }

        if (pfe.getStatus().equals(SubmissionStatus.APPROVED)) {
            // Allow upload only if rapporteur feedback exists
            if (pfe.getReporteurFeedback() == null || pfe.getReporteurFeedback().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "Rapporteur feedback is required before uploading files")
                );
            }
        }

        StudentDTO student = userServiceClient.getStudentByAuthId(String.valueOf(pfe.getStudentAuthId())).getData();
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", "Student not found")
            );
        }

        FileUploadResponse response = fileService.uploadFile(
                file,
                FileType.SUPPLEMENTARY,
                authId,
                student.getEmail(),
                "attestation",
                title,
                description,
                false
        );

        File uploadedFile = fileService.getFileById(response.getFileId());
                Long oldAttestationFileId = pfe.getAttestationFile() != null ? pfe.getAttestationFile().getId() : null;
                pfe.setAttestationFile(uploadedFile);
                pfeService.savePFE(pfe);
                if (oldAttestationFileId != null) {
                        fileService.deleteFile(oldAttestationFileId);
        }

        return ResponseEntity.ok(
                ApiResponse.success(response, "File uploaded successfully")
        );
    }



    @GetMapping("/student-PFE")
    public ResponseEntity<ApiResponse> getStudentPFE(
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-Auth-Id") Long authId,
            @RequestParam(required = false) Long studentAuthId
    ) {
        if (role.equals("STUDENT")) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            pfeService.getPFEByStudent(authId), "PFE for student auth: " + authId
                    )
            );
        }


        if (studentAuthId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("Bad Request", "Student auth ID is required for non-student users")
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        pfeService.getPFEByStudent(studentAuthId), "PFE for student auth: " + studentAuthId
                )
        );
    }



    @GetMapping("/all-PFE")
    public ResponseEntity<ApiResponse> getAllPFE(
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "Only admins can access this endpoint")
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(pfeService.getAllPFE(), "All PFE")
        );
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse> getPFEById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-Auth-Id") Long authId
    ) {
        PFE pfe = pfeService.getPFEById(id);
        if (pfe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Not Found", "PFE not found with ID: " + id)
            );
        }

        if (role.equals("STUDENT") && !pfe.getStudentAuthId().equals(authId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "You are not allowed to access this PFE")
            );
        }


        if (role.equals("TEACHER")) {
            boolean isSupervisor = pfe.getSupervisorAuthId() != null && pfe.getSupervisorAuthId().equals(authId);
            boolean isChefDeJury = pfe.getChefDeJuryAuthId() != null && pfe.getChefDeJuryAuthId().equals(authId);
            boolean isRapporteur = pfe.getRapporteurAuthId() != null && pfe.getRapporteurAuthId().equals(authId);

            if (!isSupervisor && !isChefDeJury && !isRapporteur) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.error("Forbidden", "You are not allowed to access this PFE")
                );
            }
        }

        return ResponseEntity.ok(
                ApiResponse.success(pfe, "PFE with id: " + id)
        );
    }


}