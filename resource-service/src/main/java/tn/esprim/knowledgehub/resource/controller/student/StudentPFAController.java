package tn.esprim.knowledgehub.resource.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.ApiResponse;
import tn.esprim.knowledgehub.resource.service.PFAService;

import java.util.Map;

@RestController
@RequestMapping("/api/student/pfa")
@RequiredArgsConstructor
public class StudentPFAController {

    private final PFAService pfaService;
    private final UserServiceClient userServiceClient;

    @GetMapping("/public/test")
    public ResponseEntity<ApiResponse> testFiegnClient(){

        return ResponseEntity.ok(userServiceClient.getAllClasses());
    }

    @GetMapping("/public/test/{id}")
    public ResponseEntity<ApiResponse> testFiegnClient1(@PathVariable Long id){

        return ResponseEntity.ok(userServiceClient.getClasseById(id));
    }

    @GetMapping("/public/test/student/{id}")
    public ResponseEntity<ApiResponse> testFiegnClient2(@PathVariable Long id){
        return ResponseEntity.ok(userServiceClient.getStudentById(id));
    }
}
