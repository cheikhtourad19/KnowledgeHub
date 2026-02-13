package tn.esprim.knowledgehub.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tn.esprim.knowledgehub.auth.dto.UserServiceRequest;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    public void createStudent(UserServiceRequest request) {
        try {
            String url = userServiceUrl + "/api/students/create";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UserServiceRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("Student created in user-service: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error creating student in user-service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create student in user-service: " + e.getMessage());
        }
    }

    public void createTeacher(UserServiceRequest request) {
        try {
            String url = userServiceUrl + "/api/teachers/create";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UserServiceRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("Student created in user-service: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error creating student in user-service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create student in user-service: " + e.getMessage());
        }
    }
    public void createStudents(List<UserServiceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.warn("Empty or null list provided for creating students");
            return;
        }

        try {
            String url = userServiceUrl + "/api/students/create-multiple";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<UserServiceRequest>> entity = new HttpEntity<>(requests, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("Students created in user-service: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error creating students in user-service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create students in user-service: " + e.getMessage());
        }
    }

    public void createTeachers(List<UserServiceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.warn("Empty or null list provided for creating teachers");
            return;
        }

        try {
            String url = userServiceUrl + "/api/teachers/create-multiple";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<UserServiceRequest>> entity = new HttpEntity<>(requests, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("Teachers created in user-service: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error creating teachers in user-service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create teachers in user-service: " + e.getMessage());
        }
    }

    public Long getStudentIdByAuthId(String authId) {
        try {
            String url = userServiceUrl + "/api/students/by-auth-id/" + authId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            if (data != null && data.has("studentId")) {
                return data.get("studentId").asLong();
            }
            log.warn("Student not found for authId: {}", authId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching student by authId: {}", e.getMessage());
            return null;
        }
    }

    public Long getTeacherIdByAuthId(String authId) {
        try {
            String url = userServiceUrl + "/api/teachers/by-auth-id/" + authId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            if (data != null && data.has("teacherId")) {
                return data.get("teacherId").asLong();
            }
            log.warn("Teacher not found for authId: {}", authId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching teacher by authId: {}", e.getMessage());
            return null;
        }
    }
    public Long getAdminIdByAuthId(String authId) {
        try {
            String url = userServiceUrl + "/api/admin/by-Authid/" + authId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            if (data != null && data.has("id")) {
                return data.get("id").asLong();
            }
            log.warn("admin not found for authId: {}", authId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching teacher by authId: {}", e.getMessage());
            return null;
        }
    }


}
