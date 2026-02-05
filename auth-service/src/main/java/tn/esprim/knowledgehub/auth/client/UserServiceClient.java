package tn.esprim.knowledgehub.auth.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tn.esprim.knowledgehub.auth.dto.UserServiceStudentRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    public void createStudent(UserServiceStudentRequest request) {
        try {
            String url = userServiceUrl + "/api/students/create";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UserServiceStudentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("Student created in user-service: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error creating student in user-service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create student in user-service: " + e.getMessage());
        }
    }
}
