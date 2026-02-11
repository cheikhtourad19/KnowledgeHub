package tn.esprim.knowledgehub.resource.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.repository.StageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class StageService {

    private final StageRepository stageRepository;
    private final FileService fileService;

    // TODO: Implement Stage service logic
}
