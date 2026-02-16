package tn.esprim.knowledgehub.resource.service;

import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;
import tn.esprim.knowledgehub.resource.client.dto.TeacherDTO;
import tn.esprim.knowledgehub.resource.dto.request.CreateDemandePFERequest;
import tn.esprim.knowledgehub.resource.dto.request.UpdateDemandStatusRequest;
import tn.esprim.knowledgehub.resource.dto.request.UpdatePFEStatusRequest;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.DemandePFE;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.enums.DemandStatus;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.repository.DemandePFERepository;
import tn.esprim.knowledgehub.resource.repository.PFERepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PFEService {

    private final PFERepository pfeRepository;
    private final DemandePFERepository demandePFERepository;
    private final FileService fileService;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;


    public PFE savePFE(PFE pfe) {
        return pfeRepository.save(pfe);
    }

    public PFE getPFEById(Long id) {
        return pfeRepository.findById(id).orElseThrow();
    }

    public List<PFE> getPFEByStudent(Long studentId) {
        return pfeRepository.findByStudentId(studentId);
    }

    public List<PFE> getPFEBySupervisor(Long supervisorId) {
        return pfeRepository.findBySupervisorId(supervisorId);
    }

    public List<PFE> getAllPFE() {
        return pfeRepository.findAll();
    }

    /**
     * Create a DemandePFE with file upload
     * @param request The demand request data
     * @param file The file to upload (convention de stage, etc.)
     * @param userId The user ID from X-User-Id header
     * @return The created DemandePFE
     */
    public DemandePFE createDemandePFE(CreateDemandePFERequest request, MultipartFile file, Long userId) {
        log.info("Creating DemandePFE for user ID: {}", userId);

        // Fetch student details from UserService using X-User-Id
        StudentDTO student = userServiceClient.getStudentById(userId).getData();
        if (student == null) {
            throw new RuntimeException("Student not found with ID: " + userId);
        }

        // Upload the file first
        FileUploadResponse fileUploadResponse = fileService.uploadFile(
                file,
                FileType.DEMANDE,
                userId,
                student.getNom() + " " + student.getPrenom(),
                "pfe",
                request.getPfeTitle(),
                "Demande PFE - " + request.getPfeTitle()
        );

        log.info("File uploaded successfully: {}", fileUploadResponse.getFileName());

        // Build DemandePFE entity with student data from UserService
        DemandePFE demande = DemandePFE.builder()
                .studentId(student.getStudentId())
                .studentName(student.getNom() + " " + student.getPrenom())
                .studentEmail(student.getEmail())
                .studentPhone(request.getStudentPhone())
                .student_classe(student.getClasse() != null ? student.getClasse().getClasseName() : null)
                .student_identifiant(student.getStudentId().toString())
                .student_cin(request.getStudentCin())
                .pfeTitle(request.getPfeTitle())
                .pfeDescription(request.getPfeDescription())
                .mot_cles(request.getMotCles())
                .entreprise_domaine(request.getEntrepriseDomaine())
                .entreprise_name(request.getEntrepriseName())
                .entreprise_encadrant_name(request.getEntrepriseEncadrantName())
                .entreprise_encadrant_email(request.getEntrepriseEncadrantEmail())
                .entreprise_encadrant_phone(request.getEntrepriseEncadrantPhone())
                .date_debut(request.getDateDebut())
                .date_fin(request.getDateFin())
                .build();

        DemandePFE saved = demandePFERepository.save(demande);
        log.info("DemandePFE created successfully with ID: {}", saved.getId());

        return saved;
    }

    public List<DemandePFE> getDemandePFEByStudent(Long studentId) {
        return demandePFERepository.findByStudentId(studentId);
    }

    public List<DemandePFE> getAllDemandePFE() {
        return demandePFERepository.findAll();
    }

    public DemandePFE getDemandePFEById(Long id) {
        return demandePFERepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DemandePFE not found with ID: " + id));
    }


    public DemandePFE updateDemandStatus(Long demandId, UpdateDemandStatusRequest request, Long adminId) {
        log.info("Updating demand {} status to {} by admin {}", demandId, request.getStatus(), adminId);

        DemandePFE demande = getDemandePFEById(demandId);


        if (demande.getStatus() != DemandStatus.UPLOADED) {
            throw new IllegalStateException("Can only update demands with UPLOADED status. Current status: " + demande.getStatus());
        }


        if (request.getStatus() == DemandStatus.REJECTED &&
            (request.getAdminFeedback() == null || request.getAdminFeedback().trim().isEmpty())) {
            throw new IllegalArgumentException("Admin feedback is required when rejecting a demand");
        }
        if (request.getStatus() == DemandStatus.REJECTED ) {
            notificationClient.sendNotificationAsync(
                    NotificationRequest.builder()
                            .recipientEmail(demande.getStudentEmail())
                            .type(NotificationType.EMAIL)
                            .content("Votre demande de PFE a été rejeter veillez consulter votre espace pour voir la raison et rectifier le plus vite possible")
                            .subject("Demande de PFE Rejeter")
                            .userId(demande.getStudentId().toString())
                            .build()
            );
        }

        demande.setStatus(request.getStatus());
        demande.setReviewedAt(new Date());

        DemandePFE updated = demandePFERepository.save(demande);
        log.info("Demand {} status updated to {}", demandId, request.getStatus());

        return updated;
    }


    public List<DemandePFE> getDemandePFEByStatus(DemandStatus status) {
        return demandePFERepository.findByStatus(status);
    }

    public List<Long> getTeachersInPFEandFile(Long fileId) {
        File file = fileService.getFileById(fileId);
        PFE pfe = pfeRepository.findByReportFile(file).orElse(null);
        if(pfe==null)return List.of();
        return List.of(pfe.getSupervisorId(),pfe.getStudentId(),pfe.getChefDeJuryId(),pfe.getRapporteurId());
    }


    public PFE updatePFEStatus(PFE pfe, UpdatePFEStatusRequest request, Long teacherId) {

        if (request.getStatus() == tn.esprim.knowledgehub.resource.enums.SubmissionStatus.CREATED) {
            throw new IllegalArgumentException("Cannot change status back to CREATED");
        }

        if (request.getStatus() == tn.esprim.knowledgehub.resource.enums.SubmissionStatus.REJECTED &&
            (request.getSupervisorFeedback() == null || request.getSupervisorFeedback().trim().isEmpty())) {
            throw new IllegalArgumentException("Supervisor feedback is required when rejecting a PFE submission");
        }


        pfe.setStatus(request.getStatus());
        if (request.getSupervisorFeedback() != null && !request.getSupervisorFeedback().trim().isEmpty()) {
            pfe.setSupervisorFeedback(request.getSupervisorFeedback());
        }

        PFE updated = pfeRepository.save(pfe);


        try {
            String notificationContent;
            String subject;

            switch (request.getStatus()) {
                case APPROVED -> {
                    notificationContent = "Votre PFE '" + pfe.getTitle() + "' a été approuvé par votre encadrant.";
                    subject = "PFE Approuvé";
                }
                case REJECTED -> {
                    notificationContent = "Votre PFE '" + pfe.getTitle() + "' a été rejeté. Veuillez consulter les commentaires de votre encadrant et apporter les modifications nécessaires.";
                    subject = "PFE Rejeté - Modifications Requises";
                }
                case UNDER_REVIEW -> {
                    notificationContent = "Votre PFE '" + pfe.getTitle() + "' est actuellement en cours de révision par votre encadrant.";
                    subject = "PFE En Cours de Révision";
                }
                default -> {
                    notificationContent = "Le statut de votre PFE '" + pfe.getTitle() + "' a été mis à jour.";
                    subject = "Mise à Jour du Statut du PFE";
                }
            }

            notificationClient.sendNotificationAsync(
                    NotificationRequest.builder()
                            .recipientEmail(pfe.getStudentEmail())
                            .type(NotificationType.EMAIL)
                            .content(notificationContent)
                            .subject(subject)
                            .userId(pfe.getStudentId().toString())
                            .build()
            );
            log.info("Notification sent to student {} about PFE status change", pfe.getStudentId());
        } catch (Exception e) {
            log.error("Failed to send notification to student: {}", e.getMessage());
            // Don't fail the status update if notification fails
        }

        return updated;
    }
}
