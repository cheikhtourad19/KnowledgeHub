package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.PFA;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;

import java.util.List;

@Repository
public interface PFARepository extends JpaRepository<PFA, Long> {

    List<PFA> findByStudentId(Long studentId);

    List<PFA> findBySupervisorId(Long supervisorId);

    List<PFA> findByStatus(SubmissionStatus status);

    List<PFA> findBySupervisorIdAndStatus(Long supervisorId, SubmissionStatus status);


    List<PFA> findByYear(Integer year);
}
