package tn.esprim.knowledgehub.resource.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.entity.Stage;
import tn.esprim.knowledgehub.resource.repository.PFERepository;
import tn.esprim.knowledgehub.resource.repository.StageRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccesServiceMockitoTest {

    @Mock
    private FileService fileService;

    @Mock
    private PFERepository pfeRepository;

    @Mock
    private StageRepository stageRepository;

    @InjectMocks
    private AccesService accesService;

    private File mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new File();
        mockFile.setId(1L);
        mockFile.setUploadedByAuthId(99L);
    }

    @Test
    void shouldReturnOnlyUploader() {
        when(fileService.getFileById(1L)).thenReturn(mockFile);
        when(pfeRepository.findByAnyFileId(1L)).thenReturn(Optional.empty());
        when(stageRepository.findByAnyFileId(1L)).thenReturn(Optional.empty());

        List<Long> result = accesService.fileAccess(1L);

        assertThat(result).containsExactly(99L);
    }

    @Test
    void shouldIncludeAllMembers() {
        PFE pfe = new PFE();
        pfe.setStudentAuthId(10L);
        pfe.setSupervisorAuthId(20L);
        pfe.setChefDeJuryAuthId(30L);
        pfe.setRapporteurAuthId(40L);

        when(fileService.getFileById(1L)).thenReturn(mockFile);
        when(pfeRepository.findByAnyFileId(1L)).thenReturn(Optional.of(pfe));
        when(stageRepository.findByAnyFileId(1L)).thenReturn(Optional.empty());

        List<Long> result = accesService.fileAccess(1L);

        assertThat(result).containsExactlyInAnyOrder(99L, 10L, 20L, 30L, 40L);
    }

    @Test
    void shouldIncludeStageMembers() {
        Stage stage = new Stage();
        stage.setStudentAuthId(50L);
        stage.setSupervisorAuthId(60L);

        when(fileService.getFileById(1L)).thenReturn(mockFile);
        when(pfeRepository.findByAnyFileId(1L)).thenReturn(Optional.empty());
        when(stageRepository.findByAnyFileId(1L)).thenReturn(Optional.of(stage));

        List<Long> result = accesService.fileAccess(1L);

        assertThat(result).containsExactlyInAnyOrder(99L, 50L, 60L);
    }


    @Test
    void shouldThrowException() {
        when(fileService.getFileById(999L)).thenThrow(new RuntimeException("File not found"));

        assertThatThrownBy(() -> accesService.fileAccess(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File not found");
    }
}