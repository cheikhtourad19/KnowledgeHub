package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.Stage;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;

import java.util.List;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

    List<Stage> findByStudentId(Long studentId);

    List<Stage> findBySupervisorId(Long supervisorId);




    List<Stage> findByYear(Integer year);

    List<Stage> findByCompanyName(String companyName);
}
