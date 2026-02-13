package tn.esprim.knowledgehub.resource.controller.student;



import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;
import tn.esprim.knowledgehub.resource.client.dto.TeacherDTO;
import tn.esprim.knowledgehub.resource.dto.response.ApiResponse;
import tn.esprim.knowledgehub.resource.dto.request.CreatePFARequest;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFA;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.service.FileService;
import tn.esprim.knowledgehub.resource.service.PFAService;

@RestController
@RequestMapping("/api/student/pfa")
@RequiredArgsConstructor

public class StudentPFAController {

    private final PFAService pfaService;
    private final UserServiceClient userServiceClient;
    private final UserServiceClient adminServiceClient;
    private final NotificationClient notificationClient;
    private final FileService fileService;

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
                .status(SubmissionStatus.CREATED)
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

    @PostMapping("/upload/{idPFA}")
    public ResponseEntity<ApiResponse> uploadFile(
            @PathVariable Long idPFA,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value="X-User-Id") String userId
    ){
        PFA pfa = pfaService.getPFAById(idPFA);
        StudentDTO student = userServiceClient.getStudentById(pfa.getStudentId()).getData();
        if (!pfa.getStudentId().toString().equals(userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "You are not allowed to upload files for this PFA")
            );
        }
        if (!pfa.getStatus().equals(SubmissionStatus.CREATED) && !pfa.getStatus().equals(SubmissionStatus.REJECTED)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden", "This PFA is Already approuved")
            );
        }
        FileUploadResponse response = fileService.uploadFile(
                file,
                FileType.PFA_REPORT,
                Long.parseLong(userId),
                student.getEmail(),
                "pfa",
                title,
                description
        );
        File uploaded_file = fileService.getFileById(response.getFileId());
        pfa.setReportFile(uploaded_file);
        pfa.setStatus(SubmissionStatus.SUBMITTED);
        pfaService.savePFA(pfa);
        TeacherDTO teacherDTO=userServiceClient.getTeacherById(pfa.getSupervisorId()).getData();
        notificationClient.sendNotificationAsync(
                NotificationRequest.builder()
                        .type(NotificationType.EMAIL)
                        .recipientEmail(teacherDTO.getEmail())
                        .content("en attente de validation du rapport de "+student.getNom()+" "+student.getPrenom())
                        .subject("depot de rapport")
                        .build()
        );
        return ResponseEntity.ok(
                ApiResponse.success(response,"File uploaded successfully")
        );

    }


    @GetMapping("/student-PFA")
    public ResponseEntity<ApiResponse> getStudentPFA(
            @RequestHeader(value="X-User-Role") String role
            ,@RequestHeader(value="X-User-Id") Long userId
            ,@RequestParam(required = false) Long studentId
    ){
        if (!role.equals("STUDENT")) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            pfaService.getPFAByStudent(userId),"pfa for student id: "+userId
                    )
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(
                        pfaService.getPFAByStudent(studentId),"pfa for student id: "+studentId
                )
        );

    }
    @GetMapping("/teacher-PFA")
    public ResponseEntity<ApiResponse> getTeacherPFA(
            @RequestHeader(value="X-User-Role") String role,
            @RequestHeader(value="X-User-Id") Long userId
    ){
        if (!role.equals("TEACHER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only teachers and admins can access this endpoint")
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(pfaService.getPFABySupervisor(userId),"All PFA")
        );
    }

    @GetMapping("/all-PFA")
    public ResponseEntity<ApiResponse> getAllPFA(
            @RequestHeader(value="X-User-Role") String role
    ){
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "Only admins can access this endpoint")
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(pfaService.getAllPFA(),"All PFA")
        );
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse> getPFAById(
            @PathVariable  Long id,
            @RequestHeader(value = "X-User-role") String role,
            @RequestHeader(value = "X-User-Id") Long userId
    ){
        PFA pfa=pfaService.getPFAById(id);
        if (role.equals("STUDENT") && !pfa.getStudentId().equals(userId) ){
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "You are not allowed to access this PFA")
            );
        }
        if (role.equals("TEACHER") && !pfa.getSupervisorId().equals(userId)){
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Forbidden", "You are not allowed to access this PFA")
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(pfa,"PFA with id: "+id)
        );
    }





}
