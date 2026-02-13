package tn.esprim.knowledgehub.resource.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.entity.PFA;
import tn.esprim.knowledgehub.resource.repository.PFARepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PFAService {

    private final PFARepository pfaRepository;
    private final FileService fileService;

    public PFA savePFA(PFA pfa) {
        return pfaRepository.save(pfa);
    }
    public PFA getPFAById(Long id){
        return pfaRepository.findById(id).orElseThrow();
    }
    public List<PFA> getPFAByStudent(Long studentId){
        return pfaRepository.findByStudentId(studentId);
    }
    public List<PFA> getPFABySupervisor(Long supervisorId){
        return pfaRepository.findBySupervisorId(supervisorId);
    }
    public List<PFA> getAllPFA(){
        return pfaRepository.findAll();
    }
}
