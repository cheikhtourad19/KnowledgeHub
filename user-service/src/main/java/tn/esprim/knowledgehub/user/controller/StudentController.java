package tn.esprim.knowledgehub.user.controller;


import jakarta.validation.Valid;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.user.dto.request.CreateStudentRequest;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.dto.response.StudentDTO;
import tn.esprim.knowledgehub.user.entity.Classe;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.service.StudentService;

import java.util.ArrayList;
import java.util.List;

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
                .email(request.getEmail())
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

    @PostMapping("/create-multiple")
    public ResponseEntity<ApiResponse> createMultipleStudent(@RequestBody  List<CreateStudentRequest> request

    ){



        List<Student> students = new ArrayList<>();

        for (CreateStudentRequest studentRequest : request) {

            students.add(
                    Student.builder()
                            .nom(studentRequest.getNom())
                            .authId(studentRequest.getAuthId())
                            .email(studentRequest.getEmail())
                            .prenom(studentRequest.getPrenom())
                            .build()
            );
        }


        List<Student> saved = studentService.saveStudents(students);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Student created successfully")
                        .data(saved)
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllStudents(){
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("All students retrieved successfully")
                        .data(studentService.getAllStudents())
                        .build()
        );
    }

    @GetMapping("/by-auth-id/{authId}")
    public ResponseEntity<ApiResponse> getStudentByAuthId(@PathVariable String authId) {
        Student student = studentService.getStudentByAuthId(authId);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Student retrieved successfully")
                        .data(student)
                        .build()
        );
    }
    @GetMapping("/by-id/{id}")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        StudentDTO studentDTO = StudentDTO.fromEntity(student);

        return ResponseEntity.ok(
                ApiResponse.<StudentDTO>builder()
                        .success(true)
                        .message("Student retrieved successfully")
                        .data(studentDTO)
                        .build()
        );
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<ApiResponse> getStudentByEmail(@PathVariable String email) {
        Student student = studentService.getStudentByEmail(email);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Student retrieved successfully")
                        .data(student)
                        .build()
        );
    }




}
