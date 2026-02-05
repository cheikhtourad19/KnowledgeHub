package tn.esprim.knowledgehub.user.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprim.knowledgehub.user.dto.request.CreateStudentRequest;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.service.StudentService;
import tn.esprim.knowledgehub.user.service.TeacherService;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createStudent(@RequestBody @Valid CreateStudentRequest request

    ){

        Teacher student = Teacher.builder()
                .nom(request.getNom())
                .authId(request.getAuthId())
                .prenom(request.getPrenom())
                .build();

        Teacher saved = teacherService.saveTeacher(student);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Student created successfully")
                        .data(saved)
                        .build()
        );
    }


}
