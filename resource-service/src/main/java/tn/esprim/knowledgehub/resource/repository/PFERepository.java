package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFE;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PFERepository extends JpaRepository<PFE, Long> {

    List<PFE> findByStudentId(Long studentId);

    List<PFE> findBySupervisorId(Long supervisorId);

    List<PFE> findByStatus(SubmissionStatus status);

    List<PFE> findBySupervisorIdAndStatus(Long supervisorId, SubmissionStatus status);


    List<PFE> findByYear(Integer year);

    Optional<PFE> findByReportFile(File reportFile);
    
    Optional<PFE> findByAttestationFile(File attestationFile);
}
