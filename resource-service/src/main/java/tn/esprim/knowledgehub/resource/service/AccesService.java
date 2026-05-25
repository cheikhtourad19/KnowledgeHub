package tn.esprim.knowledgehub.resource.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.entity.Stage;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.repository.PFERepository;
import tn.esprim.knowledgehub.resource.repository.StageRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccesService {

    private final FileService fileService;
    private final StageRepository stageRepository;
    private final PFERepository pfeRepository;



    public List<Long> fileAccess(Long fileId) {
        File file = fileService.getFileById(fileId);
        List<Long> usersWithAccess = new ArrayList<>();
        usersWithAccess.add(file.getUploadedByAuthId());

        pfeRepository.findByAnyFileId(fileId).ifPresent(pfe -> {
            addIfNotNull(usersWithAccess, pfe.getStudentAuthId());
            addIfNotNull(usersWithAccess, pfe.getSupervisorAuthId());
            addIfNotNull(usersWithAccess, pfe.getChefDeJuryAuthId());
            addIfNotNull(usersWithAccess, pfe.getRapporteurAuthId());
        });

        stageRepository.findByAnyFileId(fileId).ifPresent(stage -> {
            addIfNotNull(usersWithAccess, stage.getStudentAuthId());
            addIfNotNull(usersWithAccess, stage.getSupervisorAuthId());
        });

        return usersWithAccess;
    }

    private void addIfNotNull(List<Long> list, Long value) {
        if (value != null) list.add(value);
    }
}
