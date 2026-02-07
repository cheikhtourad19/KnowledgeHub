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

    @PostMapping("/assignTeacherToStudent")
    public ResponseEntity<ApiResponse> assignTeacherToStudent(@RequestBody AssignTeacherToStudentRequest request,
                                                              @RequestHeader(value = "X-User-Role") String role
    ){
      if (!role.equals("ADMIN")) {
          return ResponseEntity.status(403).body(
                  ApiResponse.builder().success(false)
                      .message("Only admins can assign teachers to students")
                      .build()
          );
      }
        try{
            Student student =teacherService.assignTeacherToStudent(request.getTeacherId(), request.getStudentId());
            return ResponseEntity.ok(
                    ApiResponse.builder().success(true)
                            .message("Teacher assigned to student successfully")
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

}
