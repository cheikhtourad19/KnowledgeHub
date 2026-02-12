package tn.esprim.knowledgehub.resource.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprim.knowledgehub.resource.client.dto.ApiResponse;
import tn.esprim.knowledgehub.resource.client.dto.ClasseDTO;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8082"
)
public interface UserServiceClient {

    @GetMapping("/api/admin/classe/")
    ApiResponse<List<ClasseDTO>> getAllClasses();
    @GetMapping("/api/admin/classe/{id}")
    ApiResponse<ClasseDTO> getClasseById(@PathVariable Long id);
    @GetMapping("/api/students/by-id/{id}")
    ApiResponse<StudentDTO> getStudentById(@PathVariable Long id);

}
