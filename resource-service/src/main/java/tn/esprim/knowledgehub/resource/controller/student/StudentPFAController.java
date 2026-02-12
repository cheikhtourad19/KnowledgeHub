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
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.request.CreatePFARequest;
import tn.esprim.knowledgehub.resource.entity.PFA;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.service.PFAService;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/student/pfa")
@RequiredArgsConstructor

public class StudentPFAController {

    private final PFAService pfaService;
    private final UserServiceClient userServiceClient;
    private final UserServiceClient adminServiceClient;
    private final NotificationClient notificationClient;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPFA(
            @RequestBody @Valid CreatePFARequest request,
            @RequestHeader(value="X-User-Role") String role

    ){
        if (!role.equals("ADMIN") && !role.equals("TEACHER")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins and teachers can create PFA")
            );
        }
        StudentDTO student = userServiceClient.getStudentById(request.getStudentId()).getData();
        TeacherDTO teacher= userServiceClient.getTeacherById(request.getSupervisorId()).getData();
        PFA pfa = PFA.builder()
                .studentId(request.getStudentId())
                .title(request.getTitle())
                .year(request.getYear())
                .status(SubmissionStatus.UNDER_REVIEW)
                .build();
        pfa.setSupervisorId(teacher.getTeacherId());
        pfa.setSupervisorName(teacher.getNom()+" "+teacher.getPrenom());
        pfa.setStudentEmail(student.getEmail());
        pfa.setStudentName(student.getNom()+" "+student.getPrenom());

        PFA saved=pfaService.savePFA(pfa);
        notificationClient.sendNotificationAsync(
                NotificationRequest.builder()
                        .type(NotificationType.EMAIL)
                        .recipientEmail(student.getEmail())
                        .subject("Vous avez été assigner un PFA")
                        .content("Vous avez été assigner un PFA veillez consulter votre espace pour plus de détails" +
                                "C'est dans cette espace que vous aurez a deposer vos document demandés")
                        .userId(student.getStudentId().toString())
                        .build()
        );
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("PFA created successfully")
                        .data(saved)
                        .build()
        );

    }
}
