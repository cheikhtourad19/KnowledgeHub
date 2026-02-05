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
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.service.TeacherService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createTeacher(@RequestBody @Valid CreateStudentRequest request

    ){

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
    public ResponseEntity<ApiResponse> createMultipleTeacher(@RequestBody  List<CreateStudentRequest> request

    ){
        List<Teacher> teachers = new ArrayList<>();
        for(CreateStudentRequest teacherRequest : request){
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

}
