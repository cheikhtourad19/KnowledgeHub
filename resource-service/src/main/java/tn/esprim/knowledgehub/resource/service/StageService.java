package tn.esprim.knowledgehub.resource.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprim.knowledgehub.resource.client.UserServiceClient;
import tn.esprim.knowledgehub.resource.client.dto.StudentDTO;
import tn.esprim.knowledgehub.resource.dto.request.StudentCreateStageRequest;
import tn.esprim.knowledgehub.resource.dto.request.StudentUpdateStageMetadataRequest;
import tn.esprim.knowledgehub.resource.dto.response.StageResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.Stage;
import tn.esprim.knowledgehub.resource.entity.StageCreationAutorisation;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.enums.TypeStage;
import tn.esprim.knowledgehub.resource.repository.StageCreationAutorisationRepository;
import tn.esprim.knowledgehub.resource.repository.StageRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StageService {

    private final StageRepository stageRepository;
    private final StageCreationAutorisationRepository stageCreationAutorisationRepository;
    private final FileService fileService;
    private final UserServiceClient userServiceClient;

    public StageService(
            StageRepository stageRepository,
            StageCreationAutorisationRepository stageCreationAutorisationRepository,
            @Lazy FileService fileService,
            UserServiceClient userServiceClient
    ) {
        this.stageRepository = stageRepository;
        this.stageCreationAutorisationRepository = stageCreationAutorisationRepository;
        this.fileService = fileService;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Autoriser un étudiant à créer un stage d'un type spécifique
     * Si une autorisation existe déjà, elle sera écrasée
     */
    @Transactional
    public void authorizeStudent(Long studentId, TypeStage typeStage){
        log.info("Authorizing student {} for stage type {}", studentId, typeStage);

        // Vérifier si une autorisation existe déjà
        Optional<StageCreationAutorisation> existingAuth = stageCreationAutorisationRepository
                .findByStudentId(studentId);

        if (existingAuth.isPresent()) {
            // Écraser l'ancienne autorisation
            StageCreationAutorisation autorisation = existingAuth.get();
            autorisation.setTypeStage(typeStage);
            stageCreationAutorisationRepository.save(autorisation);
            log.info("Updated existing authorization for student {}", studentId);
        } else {
            // Créer une nouvelle autorisation
            StageCreationAutorisation autorisation = StageCreationAutorisation.builder()
                    .studentId(studentId)
                    .typeStage(typeStage)
                    .build();
            stageCreationAutorisationRepository.save(autorisation);
            log.info("Created new authorization for student {}", studentId);
        }
    }

    /**
     * Autoriser tous les étudiants d'une classe à créer un stage
     */
    @Transactional
    public int authorizeClass(Long classId, TypeStage typeStage) {
        log.info("Authorizing all students in class {} for stage type {}", classId, typeStage);

        try {
            // Récupérer tous les étudiants de la classe via le user-service
            var response = userServiceClient.getStudentsByClassId(classId);

            if (response == null || response.getData() == null) {
                log.error("No students found for class {}", classId);
                throw new RuntimeException("Classe non trouvée ou aucun étudiant dans la classe");
            }

            List<StudentDTO> students = response.getData();
            log.info("Found {} students in class {}", students.size(), classId);

            int authorizedCount = 0;
            for (StudentDTO student : students) {
                try {
                    authorizeStudent(student.getStudentId(), typeStage);
                    authorizedCount++;
                } catch (Exception e) {
                    log.error("Error authorizing student {}: {}", student.getStudentId(), e.getMessage());
                    // Continue avec les autres étudiants
                }
            }

            log.info("Successfully authorized {} out of {} students in class {}",
                    authorizedCount, students.size(), classId);
            return authorizedCount;

        } catch (Exception e) {
            log.error("Error authorizing class {}: {}", classId, e.getMessage());
            throw new RuntimeException("Erreur lors de l'autorisation de la classe: " + e.getMessage());
        }
    }

    /**
     * Créer un stage si l'étudiant a une autorisation valide pour l'année en cours
     */
    @Transactional
    public StageResponse createStageForStudent(Long userId, StudentCreateStageRequest request) {
        log.info("Creating stage for user ID: {} with request: {}", userId, request);

        if (request.getConventionFile() == null || request.getConventionFile().isEmpty()) {
            throw new RuntimeException("La convention de stage est obligatoire");
        }

        // Récupérer l'année en cours
        int currentYear = LocalDate.now().getYear();

        // Vérifier si l'étudiant a une autorisation valide pour cette année
        Optional<StageCreationAutorisation> autorisationOpt = stageCreationAutorisationRepository
                .findByStudentId(userId);

        if (autorisationOpt.isEmpty()) {
            log.error("No authorization found for student {}", userId);
            throw new RuntimeException(
                    "Vous n'avez pas d'autorisation pour créer un stage. Veuillez contacter l'administration."
            );
        }

        StageCreationAutorisation autorisation = autorisationOpt.get();

        // Vérifier que l'autorisation est de l'année en cours
        int authYear = autorisation.getCreatedAt().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .getYear();

        if (authYear != currentYear) {
            log.error("Authorization for student {} is not for current year {} (found year {})",
                    userId, currentYear, authYear);
            throw new RuntimeException(
                    "Votre autorisation n'est pas valide pour l'année " + currentYear +
                    ". Veuillez demander une nouvelle autorisation à l'administration."
            );
        }

        // Récupérer les informations de l'étudiant depuis le user-service
        StudentDTO student;
        try {
            var response = userServiceClient.getStudentById(userId);
            if (response == null || response.getData() == null) {
                log.error("Student not found with ID: {}", userId);
                throw new RuntimeException("Étudiant non trouvé avec l'ID: " + userId);
            }
            student = response.getData();
            log.info("Retrieved student info: {} {}", student.getNom(), student.getPrenom());
        } catch (Exception e) {
            log.error("Error retrieving student info for ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des informations de l'étudiant: " + e.getMessage());
        }

        // Créer le stage avec le typeStage de l'autorisation
        Stage stage = Stage.builder()
                .title(request.getTitle())
                .companyName(request.getCompanyName())
                .durationMonths(request.getDurationMonths())
                .typeStage(autorisation.getTypeStage())
                .year(currentYear)
                .studentId(userId)
                .studentName(student.getNom() + " " + student.getPrenom())
                .studentEmail(student.getEmail())
                // supervisorId sera assigné par l'admin plus tard
                .supervisorId(null)
                .supervisorName(null)
            .cloture(false)
            .canEdit(false)
                .build();

        var uploadedConvention = fileService.uploadFile(
            request.getConventionFile(),
            FileType.CONVENTION_STAGE,
            userId,
            student.getEmail(),
            "stage",
            "Convention de stage - " + request.getTitle(),
            "Convention de stage",
            false
        );

        File conventionFile = fileService.getFileById(uploadedConvention.getFileId());
        stage.setConventionFile(conventionFile);

        Stage savedStage = stageRepository.save(stage);
        stageCreationAutorisationRepository.delete(autorisation);
        log.info("Stage created successfully with ID: {}", savedStage.getId());

        return mapToStageResponse(savedStage);
    }


    /**
     * Récupérer tous les stages (pour l'admin)
     */
    public List<StageResponse> getAllStages() {
        log.info("Retrieving all stages");
        List<Stage> stages = stageRepository.findAllWithFiles();
        return stages.stream()
                .map(this::mapToStageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les stages supervisés par un enseignant
     */
    public List<StageResponse> getStagesBySupervisor(Long supervisorId) {
        log.info("Retrieving stages for supervisor ID: {}", supervisorId);
        List<Stage> stages = stageRepository.findBySupervisorIdWithFiles(supervisorId);
        return stages.stream()
                .map(this::mapToStageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Assigner un superviseur (enseignant) à un stage
     */
    @Transactional
    public StageResponse assignSupervisor(Long stageId, Long supervisorId) {
        log.info("Assigning supervisor {} to stage {}", supervisorId, stageId);

        // Vérifier que le stage existe
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    log.error("Stage not found with ID: {}", stageId);
                    return new RuntimeException("Stage non trouvé avec l'ID: " + stageId);
                });

        // Récupérer les informations de l'enseignant depuis le user-service
        try {
            var response = userServiceClient.getTeacherById(supervisorId);
            if (response == null || response.getData() == null) {
                log.error("Teacher not found with ID: {}", supervisorId);
                throw new RuntimeException("Enseignant non trouvé avec l'ID: " + supervisorId);
            }
            var teacher = response.getData();

            // Assigner le superviseur au stage
            stage.setSupervisorId(supervisorId);
            stage.setSupervisorName(teacher.getNom() + " " + teacher.getPrenom());

            Stage updatedStage = stageRepository.save(stage);
            log.info("Successfully assigned supervisor {} to stage {}", supervisorId, stageId);

            return mapToStageResponse(updatedStage);

        } catch (Exception e) {
            log.error("Error assigning supervisor to stage {}: {}", stageId, e.getMessage());
            throw new RuntimeException("Erreur lors de l'assignation du superviseur: " + e.getMessage());
        }
    }

    @Transactional
    public StageResponse closeStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec l'ID: " + stageId));

        stage.setCloture(true);
        stage.setCanEdit(false);
        Stage updatedStage = stageRepository.save(stage);
        return mapToStageResponse(updatedStage);
    }

    @Transactional
    public StageResponse openStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec l'ID: " + stageId));

        stage.setCloture(false);
        Stage updatedStage = stageRepository.save(stage);
        return mapToStageResponse(updatedStage);
    }

    @Transactional
    public StageResponse closeStageBySupervisor(Long stageId, Long supervisorId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec l'ID: " + stageId));

        if (stage.getSupervisorId() == null || !stage.getSupervisorId().equals(supervisorId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à clôturer ce stage");
        }

        stage.setCloture(true);
        stage.setCanEdit(false);
        Stage updatedStage = stageRepository.save(stage);
        return mapToStageResponse(updatedStage);
    }

    @Transactional
    public StageResponse openStageBySupervisor(Long stageId, Long supervisorId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec l'ID: " + stageId));

        if (stage.getSupervisorId() == null || !stage.getSupervisorId().equals(supervisorId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à rouvrir ce stage");
        }

        stage.setCloture(false);
        Stage updatedStage = stageRepository.save(stage);
        return mapToStageResponse(updatedStage);
    }

    @Transactional
    public StageResponse updateCanEdit(Long stageId, boolean canEdit) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec l'ID: " + stageId));

        if (Boolean.TRUE.equals(stage.getCloture()) && canEdit) {
            throw new RuntimeException("Un stage clôturé ne peut pas être rendu modifiable");
        }

        stage.setCanEdit(canEdit);
        Stage updatedStage = stageRepository.save(stage);
        return mapToStageResponse(updatedStage);
    }

    @Transactional
    public void deleteStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec l'ID: " + stageId));
        if (stage.getReportFileId() != null) {
            fileService.deleteFile(stage.getReportFileId());
        }
        if (stage.getJournalFileId() != null) {
            fileService.deleteFile(stage.getJournalFileId());
        }
        if (stage.getAttestationFileId() != null) {
            fileService.deleteFile(stage.getAttestationFileId());
        }
        if (stage.getConventionFileId() != null) {
            fileService.deleteFile(stage.getConventionFileId());
        }
        StageCreationAutorisation autorisation=stageCreationAutorisationRepository.findByStudentId(stage.getStudentId()).orElse(null);
        if(autorisation!=null){
            stageCreationAutorisationRepository.delete(autorisation);
        }
        stageRepository.delete(stage);
    }

    @Transactional
    public StageResponse updateStageMetadataByStudent(Long stageId, Long studentId, StudentUpdateStageMetadataRequest request) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec l'ID: " + stageId));

        if (!stage.getStudentId().equals(studentId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce stage");
        }

        if (Boolean.TRUE.equals(stage.getCloture())) {
            throw new RuntimeException("Ce stage est clôturé et ne peut plus être modifié");
        }

        if (!Boolean.TRUE.equals(stage.getCanEdit())) {
            throw new RuntimeException("La modification des métadonnées est désactivée pour ce stage");
        }

        stage.setTitle(request.getTitle());
        stage.setCompanyName(request.getCompanyName());
        stage.setDurationMonths(request.getDurationMonths());

        Stage updatedStage = stageRepository.save(stage);
        return mapToStageResponse(updatedStage);
    }

    /**
     * Mapper Stage entity vers StageResponse DTO (public pour le contrôleur)
     */
    public StageResponse mapToResponse(Stage stage) {
        return mapToStageResponse(stage);
    }

    /**
     * Mapper Stage entity vers StageResponse DTO
     */
    private StageResponse mapToStageResponse(Stage stage) {
        StageResponse.StageResponseBuilder builder = StageResponse.builder()
                .id(stage.getId())
                .title(stage.getTitle())
                .companyName(stage.getCompanyName())
                .typeStage(stage.getTypeStage())
                .year(stage.getYear())
                .durationMonths(stage.getDurationMonths())
                .studentId(stage.getStudentId())
                .studentName(stage.getStudentName())
                .studentEmail(stage.getStudentEmail())
                .supervisorId(stage.getSupervisorId())
                .supervisorName(stage.getSupervisorName())
                .supervisorFeedback(stage.getSupervisorFeedback())
                .cloture(stage.getCloture())
                .canEdit(stage.getCanEdit())
                .createdDate(stage.getCreatedDate());

        // Utiliser les IDs directement pour éviter le lazy loading
        if (stage.getReportFileId() != null) {
            builder.reportFileId(stage.getReportFileId());
            // Charger l'entité complète si disponible
            if (stage.getReportFile() != null) {
                builder.reportFile(stage.getReportFile());
                builder.reportFileName(stage.getReportFile().getFileName());
            }
        }
        if (stage.getJournalFileId() != null) {
            builder.journalFileId(stage.getJournalFileId());
            if (stage.getJournalFile() != null) {
                builder.journalFile(stage.getJournalFile());
                builder.journalFileName(stage.getJournalFile().getFileName());
            }
        }
        if (stage.getAttestationFileId() != null) {
            builder.attestationFileId(stage.getAttestationFileId());
            if (stage.getAttestationFile() != null) {
                builder.attestationFile(stage.getAttestationFile());
                builder.attestationFileName(stage.getAttestationFile().getFileName());
            }
        }
        if (stage.getConventionFileId() != null) {
            builder.conventionFileId(stage.getConventionFileId());
            if (stage.getConventionFile() != null) {
                builder.conventionFile(stage.getConventionFile());
                builder.conventionFileName(stage.getConventionFile().getFileName());
            }
        }

        return builder.build();
    }

     /**
     * Get Stage by ID with all files eagerly loaded
     */
    public Stage getStageById(Long id) {
        return stageRepository.findByIdWithFiles(id).orElse(null);
    }

    public void deleteAllDemande(){
        stageCreationAutorisationRepository.deleteAll();
    }

    /**
     * Get all stages for a specific student
     */
    public List<StageResponse> getStagesByStudentId(Long studentId) {
        log.info("Retrieving stages for student ID: {}", studentId);
        List<Stage> stages = stageRepository.findByStudentIdWithFiles(studentId);
        return stages.stream()
                .map(this::mapToStageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get student's stage authorization
     */
    public StageCreationAutorisation getStudentAuthorization(Long studentId) {
        log.info("Checking authorization for student ID: {}", studentId);
        return stageCreationAutorisationRepository.findByStudentId(studentId).orElse(null);
    }
}

