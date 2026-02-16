package tn.esprim.knowledgehub.user.controller;


import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.user.dto.request.StudentToClassRequest;
import tn.esprim.knowledgehub.user.dto.request.StudentsToClassRequest;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.entity.Classe;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.repository.AdminRepository;
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
    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/by-Authid/{authId}")
    public ResponseEntity<ApiResponse> getAdminById(@PathVariable String authId){
        return ResponseEntity.ok(
                ApiResponse.builder().success(true).data(adminRepository.findByAuthId(authId).orElse(null)).build()
        );
    }

    @GetMapping("/classe")
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
            @RequestBody StudentsToClassRequest request,
            @RequestHeader(value="X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).build();

        }
        Long classId= request.getClassId();
        List<Long> studentIds = request.getStudentId();
        List<Student> students = new ArrayList<>();
        for (Long studentId : studentIds) {
            try {
               students.add( studentService.addStudentToClasse(studentId, classId));

            } catch (Exception e) {
                continue;
            }
        }

        return ResponseEntity.ok(
                ApiResponse.builder().success(true)
                        .message("Students added to classe successfully")
                        .data(students)
                        .build()
        );

    }







    }
