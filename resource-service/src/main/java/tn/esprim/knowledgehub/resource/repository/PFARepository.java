package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.entity.PFA;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PFARepository extends JpaRepository<PFA, Long> {

    List<PFA> findByStudentAuthId(Long studentAuthId);

    List<PFA> findBySupervisorAuthId(Long supervisorAuthId);

    List<PFA> findByStatus(SubmissionStatus status);

    List<PFA> findBySupervisorAuthIdAndStatus(Long supervisorAuthId, SubmissionStatus status);


    List<PFA> findByYear(Integer year);
    
    Optional<PFA> findByReportFile(File reportFile);
}
