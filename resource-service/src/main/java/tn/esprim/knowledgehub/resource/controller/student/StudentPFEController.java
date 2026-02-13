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
import tn.esprim.knowledgehub.resource.dto.request.CreatePFERequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.service.FileService;
import tn.esprim.knowledgehub.resource.service.PFEService;

@RestController
@RequestMapping("/api/student/pfe")
@RequiredArgsConstructor
public class StudentPFEController {

    private final PFEService pfeService;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;
    private final FileService fileService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPFE(
            @RequestBody @Valid CreatePFERequest request,
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN") && !role.equals("TEACHER")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins and teachers can create PFE")
            );
        }
        StudentDTO student = userServiceClient.getStudentById(request.getStudentId()).getData();
        TeacherDTO supervisor = userServiceClient.getTeacherById(request.getSupervisorId()).getData();

        PFE pfe = PFE.builder()
                .studentId(request.getStudentId())
                .title(request.getTitle())
                .year(request.getYear())
                .status(SubmissionStatus.UNDER_REVIEW)
                .build();

        pfe.setSupervisorId(supervisor.getTeacherId());
        pfe.setSupervisorName(supervisor.getNom() + " " + supervisor.getPrenom());
        pfe.setStudentEmail(student.getEmail());
        pfe.setStudentName(student.getNom() + " " + student.getPrenom());

        if (request.getChefDeJuryId() != null) {
            TeacherDTO chef = userServiceClient.getTeacherById(request.getChefDeJuryId()).getData();
            pfe.setChefDeJuryId(chef.getTeacherId());
            pfe.setChefDeJuryName(chef.getNom() + " " + chef.getPrenom());
        }

        if (request.getRapporteurId() != null) {
            TeacherDTO rapporteur = userServiceClient.getTeacherById(request.getRapporteurId()).getData();
            pfe.setRapporteurId(rapporteur.getTeacherId());
            pfe.setRapporteurName(rapporteur.getNom() + " " + rapporteur.getPrenom());
        }

        PFE saved = pfeService.savePFE(pfe);

        notificationClient.sendNotificationAsync(
                NotificationRequest.builder()
                        .type(NotificationType.EMAIL)
                        .recipientEmail(student.getEmail())
                        .subject("Vous avez été assigné un PFE")
                        .content("Vous avez été assigné un PFE, veuillez consulter votre espace pour plus de détails. C'est dans cet espace que vous aurez à déposer vos documents demandés.")
                        .userId(student.getStudentId().toString())
                        .build()
        );

        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("PFE created successfully")
                        .data(saved)
                        .build()
        );

    }

    @PostMapping("/upload/{idPFE}")
    public ResponseEntity<ApiResponse> uploadFile(
            @PathVariable Long idPFE,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id") String userId
    ) {
        PFE pfe = pfeService.getPFEById(idPFE);
        StudentDTO student = userServiceClient.getStudentById(pfe.getStudentId()).getData();

        if (!pfe.getStudentId().toString().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "You are not allowed to upload files for this PFE")
            );
        }

        if (!pfe.getStatus().equals(SubmissionStatus.UNDER_REVIEW) && !pfe.getStatus().equals(SubmissionStatus.REJECTED)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "This PFE is already approved")
            );
        }

        FileUploadResponse response = fileService.uploadFile(
                file,
                FileType.PFE_REPORT,
                Long.parseLong(userId),
                student.getEmail(),
                "pfe",
                title,
                description
        );

        File uploadedFile = fileService.getFileById(response.getFileId());
        pfe.setReportFile(uploadedFile);
        pfe.setStatus(SubmissionStatus.SUBMITTED);
        pfeService.savePFE(pfe);

        TeacherDTO teacherDTO = userServiceClient.getTeacherById(pfe.getSupervisorId()).getData();
        notificationClient.sendNotificationAsync(
                NotificationRequest.builder()
                        .type(NotificationType.EMAIL)
                        .recipientEmail(teacherDTO.getEmail())
                        .content("En attente de validation du rapport de " + student.getNom() + " " + student.getPrenom())
                        .subject("Dépôt de rapport PFE")
                        .build()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "File uploaded successfully")
        );
    }

    @GetMapping("/student-PFE")
    public ResponseEntity<ApiResponse> getStudentPFE(
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") Long userId,
            @RequestParam(required = false) Long studentId
    ) {

        if (role.equals("STUDENT")) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            pfeService.getPFEByStudent(userId), "PFE for student id: " + userId
                    )
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(
                        pfeService.getPFEByStudent(studentId), "PFE for student id: " + studentId
                )
        );
    }

    @GetMapping("/teacher-PFE")
    public ResponseEntity<ApiResponse> getTeacherPFE(
            @RequestHeader(value = "X-User-Role") String role,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        if (!role.equals("TEACHER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only teachers and admins can access this endpoint")
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(pfeService.getPFEBySupervisor(userId), "All PFE")
        );
    }

    @GetMapping("/all-PFE")
    public ResponseEntity<ApiResponse> getAllPFE(
            @RequestHeader(value = "X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
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
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        PFE pfe = pfeService.getPFEById(id);
        if (role.equals("STUDENT") && !pfe.getStudentId().equals(userId)) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "You are not allowed to access this PFE")
            );
        }
        if (role.equals("TEACHER") && !pfe.getSupervisorId().equals(userId)
                && (pfe.getChefDeJuryId() == null || !pfe.getChefDeJuryId().equals(userId))
                && (pfe.getRapporteurId() == null || !pfe.getRapporteurId().equals(userId))) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "You are not allowed to access this PFE")
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(pfe, "PFE with id: " + id)
        );
    }

}
