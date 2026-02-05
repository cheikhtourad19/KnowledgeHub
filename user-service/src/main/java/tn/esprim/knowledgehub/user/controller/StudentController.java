package tn.esprim.knowledgehub.user.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.user.dto.request.CreateStudentRequest;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.entity.Classe;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createStudent(@RequestBody @Valid CreateStudentRequest request

    ){

        Student student = Student.builder()
                .nom(request.getNom())
                .authId(request.getAuthId())
                .prenom(request.getPrenom())
                .build();

        Student saved = studentService.saveStudent(student);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Student created successfully")
                        .data(saved)
                        .build()
        );
    }


}
