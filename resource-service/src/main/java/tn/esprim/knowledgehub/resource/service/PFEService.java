package tn.esprim.knowledgehub.resource.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.repository.PFERepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PFEService {

    private final PFERepository pfeRepository;
    private final FileService fileService;

    // TODO: Implement PFE service logic
}
