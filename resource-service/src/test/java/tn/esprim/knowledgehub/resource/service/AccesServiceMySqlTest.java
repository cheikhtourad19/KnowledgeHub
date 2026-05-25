package tn.esprim.knowledgehub.resource.service;


import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.entity.Stage;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import tn.esprim.knowledgehub.resource.repository.PFERepository;
import tn.esprim.knowledgehub.resource.repository.StageRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccesServiceMySqlTest {

    @Autowired
    private AccesService accesService;

    @Autowired
    private PFERepository pfeRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private FileService fileService;

    private File savedFile;

    @BeforeEach
    void setUp() {
        File file = File.builder()
                .uploadedByAuthId(99L)
                .fileName("test-file.pdf")
                .originalFileName("test-file.pdf")
                .fileSize(1024L)
                .storagePath("/storage/test-file.pdf")
                .fileType(FileType.PFE_REPORT)
                .isPublished(false)
                .isScanned(false)
                .build();

        savedFile = fileService.saveFile(file);
    }

    @Test
    void shouldReturnOnlyUploader() {
        List<Long> result = accesService.fileAccess(savedFile.getId());
        assertThat(result).containsExactly(99L);
    }

    @Test
    void shouldReturnAllPFEMembers() {
        PFE pfe = new PFE();
        pfe.setReportFile(savedFile);
        pfe.setStudentAuthId(10L);
        pfe.setSupervisorAuthId(20L);
        pfe.setChefDeJuryAuthId(30L);
        pfe.setRapporteurAuthId(40L);
        pfe.setTitle("Test PFE");
        pfe.setYear(2026);
        pfe.setStatus(SubmissionStatus.APPROVED);
        pfeRepository.save(pfe);

        List<Long> result = accesService.fileAccess(savedFile.getId());

        assertThat(result).containsExactlyInAnyOrder( 10L, 20L, 30L, 40L, 99L);
    }

    @Test
    void shouldReturnStageMembers() {
        Stage stage = new Stage();
        stage.setReportFile(savedFile);
        stage.setStudentAuthId(50L);
        stage.setSupervisorAuthId(60L);
        stage.setTitle("Test Stage");
        stage.setYear(2026);

        stageRepository.save(stage);

        List<Long> result = accesService.fileAccess(savedFile.getId());

        assertThat(result).containsExactlyInAnyOrder( 50L, 60L,99L);
    }


}