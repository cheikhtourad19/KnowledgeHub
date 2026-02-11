package tn.esprim.knowledgehub.resource.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.repository.PFARepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PFAService {

    private final PFARepository pfaRepository;
    private final FileService fileService;

    // TODO: Implement PFA service logic
}
