package tn.esprim.knowledgehub.resource.service;

import com.knowledgehub.notification.client.NotificationClient;
import com.knowledgehub.notification.client.dto.NotificationRequest;
import com.knowledgehub.notification.client.dto.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
@Slf4j
public class PFEService {

    private final PFERepository pfeRepository;
    private final DemandePFERepository demandePFERepository;
    private final FileService fileService;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;

    public PFEService(
            PFERepository pfeRepository,
            DemandePFERepository demandePFERepository,
            @Lazy FileService fileService,
            UserServiceClient userServiceClient,
            NotificationClient notificationClient) {
        this.pfeRepository = pfeRepository;
        this.demandePFERepository = demandePFERepository;
        this.fileService = fileService;
        this.userServiceClient = userServiceClient;
        this.notificationClient = notificationClient;
    }




    public PFE getPFEByFile(Long FileId) {
        File file = fileService.getFileById(FileId);
        return pfeRepository.findByReportFile(file)
                .orElseGet(() -> pfeRepository.findByAttestationFile(file).orElse(null));

    }
    public List<PFE> getPFEByJury(Long juryId){
        return this.pfeRepository.findByChefDeJuryId(juryId);
    }
    public DemandePFE getDemandePFEByStudentId(Long studentId) {
        return this.demandePFERepository.findByStudentId(studentId);
    }
    public void removeDemandePFE(Long id){
        DemandePFE demandePFE = this.demandePFERepository.findById(id).orElseThrow();
        this.demandePFERepository.deleteById(demandePFE.getId());
        fileService.deleteFile(demandePFE.getAttestationFile().getId());
    }

    @Transactional
    public void deletePFEWithDemande(Long pfeId) {
        PFE pfe = pfeRepository.findById(pfeId)
                .orElseThrow(() -> new EntityNotFoundException("PFE not found: " + pfeId));

        DemandePFE demande = demandePFERepository.findByStudentId(pfe.getStudentId());
        if (demande != null) {
            if (demande.getAttestationFile() != null)
                fileService.deleteFile(demande.getAttestationFile().getId());
            demandePFERepository.delete(demande);
        }


        if (pfe.getReportFile() != null) fileService.deleteFile(pfe.getReportFile().getId());
        if (pfe.getAttestationFile() != null) fileService.deleteFile(pfe.getAttestationFile().getId());


        pfeRepository.delete(pfe);
    }

    public List<PFE> getPFEByRapporteur(Long rapporteurId){
        return this.pfeRepository.findByRapporteurId(rapporteurId);
    }

    public List<File> getFilesByPFE(Long pfeId) {
        PFE pfe = pfeRepository.findById(pfeId).orElseThrow();
        List<File> files = List.of(pfe.getReportFile(), pfe.getAttestationFile());
        return files;

    }

    public PFE savePFE(PFE pfe) {


        if (pfe.getId() != null) {
            return pfeRepository.save(pfe);
        }
        
        TeacherDTO teacher = userServiceClient.getTeacherById(pfe.getSupervisorId()).getData();
        notificationClient.sendNotificationAsync(
                NotificationRequest.builder()
                        .recipientEmail(teacher.getEmail())
                        .type(NotificationType.EMAIL)
                        .subject("Un PFE vous a été assigné")
                        .content("Un PFE vous a été assigné en tant qu'encadrant, vous pouvez suivre son avancement et consulter les depots de rapport de PFE")
                        .userId(pfe.getStudentId().toString())
                        .build()
        );
        notificationClient.sendNotificationAsync(
                NotificationRequest.builder()
                        .recipientEmail(pfe.getStudentEmail())
                        .type(NotificationType.EMAIL)
                        .subject("Creation d'un depot de PFE")
                        .content("Apres examination de votre demande un espace de depot de PFE vous a été créé, vous pouvez y deposer votre rapport de PFE et suivre son avancement")
                        .userId(pfe.getStudentId().toString())
                        .build()
        );

        return pfeRepository.save(pfe);
    }
    @Transactional
    public void removeDemande(Long id){
        DemandePFE demande = this.demandePFERepository.findById(id).orElseThrow();
        if (demande != null) {
            if (demande.getAttestationFile() != null)
                fileService.deleteFile(demande.getAttestationFile().getId());
            demandePFERepository.delete(demande);
        }

    }
    public void removeAllDemandes(){
        demandePFERepository.deleteAll();
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
     * @param request The demand request data (only fields student needs to provide)
     * @param file The file to upload (convention de stage, etc.)
     * @param userId The user ID from X-User-Id header (JWT token)
     * @return The created DemandePFE
     */
    public DemandePFE createDemandePFE(CreateDemandePFERequest request, MultipartFile file, Long userId) {
        log.info("Creating DemandePFE for user ID: {}", userId);

        // Fetch student details from UserService using X-User-Id from JWT
        StudentDTO student = userServiceClient.getStudentById(userId).getData();
        if (student == null) {
            throw new RuntimeException("Student not found with ID: " + userId);
        }

        // Build student name from UserService data
        String studentName = student.getNom() + " " + student.getPrenom();
        String studentClasse = (student.getClasse() != null) ? student.getClasse().getClasseName() : null;

        // Upload the file first
        FileUploadResponse fileUploadResponse = fileService.uploadFile(
                file,
                FileType.DEMANDE,
                userId,
                studentName,
                "pfe",
                request.getPfeTitle(),
                "Demande PFE - " + request.getPfeTitle(),
                false
        );

        log.info("File uploaded successfully: {}", fileUploadResponse.getFileName());

        // Retrieve the File entity to set the relationship
        File uploadedFile = fileService.getFileById(fileUploadResponse.getFileId());

        // Build DemandePFE entity with data from UserService (JWT) and form request
        DemandePFE demande = DemandePFE.builder()
                // Student info from UserService (extracted via X-User-Id)
                .studentId(student.getStudentId())
                .studentName(studentName)
                .studentEmail(student.getEmail())
                .studentClasse(studentClasse)
                // Student info from form (not available in UserService)
                .studentCin(request.getStudentCin())
                .studentPhone(request.getStudentPhone())
                // PFE details from form
                .pfeTitle(request.getPfeTitle())
                .pfeDescription(request.getPfeDescription())
                .motCles(request.getMotCles())
                // Company details from form
                .entrepriseDomaine(request.getEntrepriseDomaine())
                .entrepriseName(request.getEntrepriseName())
                .entrepriseEncadrantName(request.getEntrepriseEncadrantName())
                .entrepriseEncadrantEmail(request.getEntrepriseEncadrantEmail())
                .entrepriseEncadrantPhone(request.getEntrepriseEncadrantPhone())
                // Dates from form
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                // File relationship
                .attestationFile(uploadedFile)
                .build();

        DemandePFE saved = demandePFERepository.save(demande);
        log.info("DemandePFE created successfully with ID: {}", saved.getId());

        return saved;
    }

    public DemandePFE getDemandePFEByStudent(Long studentId) {
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

        // Send notification if rejected
        if (request.getStatus() == DemandStatus.REJECTED ) {
            notificationClient.sendNotificationAsync(
                    NotificationRequest.builder()
                            .recipientEmail(demande.getStudentEmail())
                            .type(NotificationType.EMAIL)
                            .content("Votre demande de PFE a été rejetée. Veuillez consulter votre espace pour voir la raison et rectifier le plus vite possible")
                            .subject("Demande de PFE Rejetée")
                            .userId(demande.getStudentId().toString())
                            .build()
            );
        }

        // Update demand with status and admin review information
        demande.setStatus(request.getStatus());
        demande.setAdminFeedback(request.getAdminFeedback());
        demande.setReviewedByAdminId(adminId);
        // Note: reviewedByAdminName should be fetched from UserService if needed
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
        
        // Check if file is a PFE report file
        PFE pfe = pfeRepository.findByReportFile(file).orElse(null);
        
        // If not found, check if file is a PFE attestation file
        if (pfe == null) {
            pfe = pfeRepository.findByAttestationFile(file).orElse(null);
        }
        
        if(pfe==null)return List.of();
        
        // Filter out null values to prevent NullPointerException with List.of()
        List<Long> userIds = new java.util.ArrayList<>();
        if (pfe.getSupervisorId() != null) userIds.add(pfe.getSupervisorId());
        if (pfe.getStudentId() != null) userIds.add(pfe.getStudentId());
        if (pfe.getChefDeJuryId() != null) userIds.add(pfe.getChefDeJuryId());
        if (pfe.getRapporteurId() != null) userIds.add(pfe.getRapporteurId());
        
        return userIds;
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

    public PFE addRapporteurFeedback(PFE pfe, String feedback) {
        if (pfe.getRapporteurId() == null) {
            throw new IllegalStateException("No rapporteur assigned to this PFE");
        }

        pfe.setReporteurFeedback(feedback);
        return pfeRepository.save(pfe);
    }

    public PFE closePFE(PFE pfe) {
        if (Boolean.TRUE.equals(pfe.getCloture())) {
            throw new IllegalStateException("PFE is already closed");
        }

        pfe.setCloture(true);
        return pfeRepository.save(pfe);
    }

    public PFE cancelPFECloture(PFE pfe) {
        if (!Boolean.TRUE.equals(pfe.getCloture())) {
            throw new IllegalStateException("PFE is not closed");
        }

        pfe.setCloture(false);
        return pfeRepository.save(pfe);
    }

    public void deletePFE(Long id) {
        PFE pfe = pfeRepository.findById(id).orElseThrow();
        pfeRepository.deleteById(pfe.getId());
        if (pfe.getReportFile() !=null){
            fileService.deleteFile(pfe.getReportFile().getId());
        }
        if (pfe.getAttestationFile() !=null){
            fileService.deleteFile(pfe.getAttestationFile().getId());

        }
    }
}
