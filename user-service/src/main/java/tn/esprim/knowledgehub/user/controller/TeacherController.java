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

    @PostMapping("/assignTome/{id}")
    public ResponseEntity<ApiResponse> assignStudent(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                     @RequestHeader(value = "X-User-Id") Long teacherId,@PathVariable Long id){
        if (!role.equals("TEACHER")) {
            return ResponseEntity.status(403).body(
                    ApiResponse.builder().success(false)
                            .message("Only teachers can assign students to themselves")
                            .build()
            );
        }
        Long id_teacher = teacherService.getTeacherByAuthId(teacherId).getTeacherId();

        try{

            Student student = teacherService.assignTeacherToStudent(id_teacher, id);
            log.info("Teacher {} assigning student {}",teacherId,id);
            return ResponseEntity.ok(
                    ApiResponse.builder().success(true)
                            .message("Student assigned successfully")
                            .data(student)
                            .build()
            );
        }catch (Exception e){
            return ResponseEntity.status(400).body(
                    ApiResponse.builder().success(false)
                            .message(e.getMessage())
                            .build()
            );
        }

    }
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
