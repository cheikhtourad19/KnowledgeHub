package tn.esprim.knowledgehub.user.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.user.dto.request.AssignTeacherToStudentRequest;
import tn.esprim.knowledgehub.user.dto.response.ApiResponse;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.service.StudentService;
import tn.esprim.knowledgehub.user.service.TeacherService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;








}
