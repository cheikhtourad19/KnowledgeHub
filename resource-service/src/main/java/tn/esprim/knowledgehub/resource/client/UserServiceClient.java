package tn.esprim.knowledgehub.resource.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprim.knowledgehub.resource.client.dto.ApiResponse;
import tn.esprim.knowledgehub.resource.client.dto.ClasseDTO;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;
import tn.esprim.knowledgehub.resource.client.dto.TeacherDTO;

import java.util.List;

@FeignClient(
        name = "user-service",
        url = "${user.service.url:http://localhost:8082}"  // Fixed to match YAML
)
public interface UserServiceClient {

    @GetMapping("/api/admin/classe/")
    ApiResponse<List<ClasseDTO>> getAllClasses();

    @GetMapping("/api/admin/classe/{id}")
    ApiResponse<ClasseDTO> getClasseById(@PathVariable Long id);

    @GetMapping("/api/students/by-id/{id}")
    ApiResponse<StudentDTO> getStudentById(@PathVariable Long id);

    @GetMapping("/api/admin/classe/{classId}/students")
    ApiResponse<List<StudentDTO>> getStudentsByClassId(@PathVariable Long classId);

    @GetMapping("/api/teachers/by-id/{id}")
    ApiResponse<TeacherDTO> getTeacherById(@PathVariable Long id);
}