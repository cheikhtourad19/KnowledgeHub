package tn.esprim.knowledgehub.resource.repository;

import org.apache.el.stream.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.DemandePFE;
import tn.esprim.knowledgehub.resource.enums.DemandStatus;

import java.util.List;

@Repository
public interface DemandePFERepository extends JpaRepository<DemandePFE, Long> {
    DemandePFE findByStudentId(Long studentId);
    List<DemandePFE> findByStudentEmail(String email);
    List<DemandePFE> findByStatus(DemandStatus status);

}
