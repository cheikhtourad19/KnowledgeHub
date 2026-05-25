package tn.esprim.knowledgehub.resource.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFA;
import tn.esprim.knowledgehub.resource.repository.PFARepository;

import java.util.ArrayList;
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
    public List<PFA> getPFAByStudent(Long authId){
        return pfaRepository.findByStudentAuthId(authId);
    }
    public List<PFA> getPFABySupervisor(Long authId){
        return pfaRepository.findBySupervisorAuthId(authId);
    }
    public List<PFA> getAllPFA(){
        return pfaRepository.findAll();
    }
    
    public List<Long> usersWithAccess(Long fileId) {
        File file = fileService.getFileById(fileId);
        PFA pfa = pfaRepository.findByReportFile(file).orElse(null);
        if (pfa == null) return List.of();
        
        List<Long> userIds = new ArrayList<>();
        if (pfa.getSupervisorAuthId() != null) userIds.add(pfa.getSupervisorAuthId());
        if (pfa.getStudentAuthId() != null) userIds.add(pfa.getStudentAuthId());
        
        return userIds;
    }
}
