package tn.esprim.knowledgehub.resource.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {


    @GetMapping("/api/students/{studentId}")
    Object getStudentById(@PathVariable Long studentId);

    @GetMapping("/api/teachers/{teacherId}")
    Object getTeacherById(@PathVariable Long teacherId);
}
