package tn.esprim.knowledgehub.user.controller;


import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.user.dto.request.StudentToClassRequest;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.entity.Classe;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.service.StudentService;
import tn.esprim.knowledgehub.user.service.TeacherService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;

    @GetMapping("/classe/")
    public ResponseEntity<ApiResponse> getAllClasses(){
        return ResponseEntity.ok(
                ApiResponse.builder().success(true).data(studentService.getAllClasses()).build()
        );

    }


    @GetMapping("/classe/{id}")
    public ResponseEntity<ApiResponse> getClasseById(@PathVariable Long id){
        Classe classe = studentService.getClasseById(id);
        if (classe.getStudents() != null) {
            Hibernate.initialize(classe.getStudents());
        }
        return ResponseEntity.ok(
                ApiResponse.builder().success(true).data(classe).build()
        );
    }

    @PostMapping("/add-student-classe")
    public ResponseEntity<ApiResponse> addStudentToClasse(
            @RequestBody StudentToClassRequest request,
            @RequestHeader(value="X-User-Role") String role
            )
    {
        if(!role.equals("ADMIN"))
        {
            return ResponseEntity.status(403).build();
        }
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .data(
                                    studentService.addStudentToClasse(request.getStudentId(),request.getClassId())
                            )
                            .message("Student added to classe successfully")
                            .build()
            );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().success(false).message(e.getMessage()).build()
            );
        }


    }

    @PostMapping("/add-students-classes")
    public ResponseEntity<ApiResponse> addStudentsClasses(
            @RequestBody List<StudentToClassRequest> request,
            @RequestHeader(value="X-User-Role") String role
    ){
        if (!role.equals("ADMIN")){
            return ResponseEntity.status(403).build();

        }
        List<String> non_added_students= new ArrayList<>();
        List<Student> students = new ArrayList<>();
        for (StudentToClassRequest studentToClassRequest : request) {
            try {
                 students.add(studentService.addStudentToClasse(studentToClassRequest.getStudentId(),studentToClassRequest.getClassId()));
             }catch (Exception e){
                 non_added_students.add(e.getMessage());
             }
        }
        boolean success = non_added_students.isEmpty();

        String message = success
                ? "all students were added to their classe successfully"
                : "some students were not added to classe: " + non_added_students;

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(success)
                        .message(message)
                        .data(students)
                        .build()
        );}








    }
