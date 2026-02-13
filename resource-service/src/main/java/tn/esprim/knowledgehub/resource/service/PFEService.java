package tn.esprim.knowledgehub.resource.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.repository.PFERepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PFEService {

    private final PFERepository pfeRepository;
    private final FileService fileService;

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
}
