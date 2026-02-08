package tn.esprim.knowledgehub.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.auth.client.UserServiceClient;
import tn.esprim.knowledgehub.auth.dto.UserServiceRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserServiceClient userServiceClient;


    @Async("userProfileTaskExecutor")
    public void createStudentProfileAsync(String authId, String prenom, String nom) {
        try {
            log.info("Async creating student profile for authId: {}", authId);

            UserServiceRequest request = UserServiceRequest.builder()
                    .authId(authId)
                    .prenom(prenom)
                    .nom(nom)
                    .build();

            userServiceClient.createStudent(request);
            log.info("Student profile created successfully for authId: {}", authId);

        } catch (Exception e) {
            log.error("Failed to create student profile for authId: {}", authId, e);
            // TODO: Could implement retry logic or save to a retry queue
        }
    }

    /**
     * dima asynchrone 5err
     */
    @Async("userProfileTaskExecutor")
    public void createTeacherProfileAsync(String authId, String prenom, String nom) {
        try {
            log.info("Async creating teacher profile for authId: {}", authId);

            UserServiceRequest request = UserServiceRequest.builder()
                    .authId(authId)
                    .prenom(prenom)
                    .nom(nom)
                    .build();

            userServiceClient.createTeacher(request);
            log.info("Teacher profile created successfully for authId: {}", authId);

        } catch (Exception e) {
            log.error("Failed to create teacher profile for authId: {}", authId, e);
            // TODO: Could implement retry logic or save to a retry queue
        }
    }

    /**
     * dima asynchrone 5err
     */
    @Async("userProfileTaskExecutor")
    public void createStudentProfilesAsync(List<UserServiceRequest> requests) {
        try {
            log.info("Async creating {} student profiles", requests.size());

            userServiceClient.createStudents(requests);
            log.info("Successfully created {} student profiles", requests.size());

        } catch (Exception e) {
            log.error("Failed to create student profiles in bulk", e);
            // TODO: Could implement retry logic or save to a retry queue
        }
    }

    /**
     * dima asynchrone 5err
     */
    @Async("userProfileTaskExecutor")
    public void createTeacherProfilesAsync(List<UserServiceRequest> requests) {
        try {
            log.info("Async creating {} teacher profiles", requests.size());

            userServiceClient.createTeachers(requests);
            log.info("Successfully created {} teacher profiles", requests.size());

        } catch (Exception e) {
            log.error("Failed to create teacher profiles in bulk", e);
            // TODO: Could implement retry logic or save to a retry queue
        }
    }
}
