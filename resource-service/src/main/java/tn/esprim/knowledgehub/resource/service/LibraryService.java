package tn.esprim.knowledgehub.resource.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.repository.DocumentRepository;
import tn.esprim.knowledgehub.resource.repository.PFARepository;
import tn.esprim.knowledgehub.resource.repository.PFERepository;
import tn.esprim.knowledgehub.resource.repository.StageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {

    private final PFARepository pfaRepository;
    private final PFERepository pfeRepository;
    private final StageRepository stageRepository;
    private final DocumentRepository documentRepository;

    // TODO: Implement Library service logic
}
