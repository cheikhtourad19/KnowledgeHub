package tn.esprim.knowledgehub.user.controller;


import jakarta.validation.Valid;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.user.dto.request.CreateStudentRequest;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.service.StudentService;
import tn.esprim.knowledgehub.user.service.TeacherService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createTeacher(@RequestBody @Valid CreateStudentRequest request

    ) {

        Teacher student = Teacher.builder()
                .nom(request.getNom())
                .authId(request.getAuthId())
                .prenom(request.getPrenom())
                .build();

        Teacher saved = teacherService.saveTeacher(student);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Teachers created successfully")
                        .data(saved)
                        .build()
        );
    }

    @PostMapping("/create-multiple")
    public ResponseEntity<ApiResponse> createMultipleTeacher(@RequestBody List<CreateStudentRequest> request

    ) {
        List<Teacher> teachers = new ArrayList<>();
        for (CreateStudentRequest teacherRequest : request) {
            teachers.add(
                    Teacher.builder()
                            .nom(teacherRequest.getNom())
                            .authId(teacherRequest.getAuthId())
                            .prenom(teacherRequest.getPrenom())
                            .build()
            );
        }
        List<Teacher> saved = teacherService.saveTeachers(teachers);
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Teachers created successfully")
                        .data(saved)
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllTeachers() {
        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("All students retrieved successfully")
                        .data(teacherService.getAllTeachers())
                        .build()
        );
    }


}
