package tn.esprim.knowledgehub.resource.controller.student;

import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;
import tn.esprim.knowledgehub.resource.client.dto.TeacherDTO;
import tn.esprim.knowledgehub.resource.dto.request.CreatePFERequest;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.service.PFEService;

@RestController
@RequestMapping("/api/student/pfe")
@RequiredArgsConstructor
public class StudentPFEController {

    private final PFEService pfeService;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;

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

    @GetMapping("/{id}")
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
