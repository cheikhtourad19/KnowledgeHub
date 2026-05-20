package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprim.knowledgehub.resource.entity.StageCreationAutorisation;
import tn.esprim.knowledgehub.resource.enums.TypeStage;

import java.util.List;
import java.util.Optional;

public interface StageCreationAutorisationRepository extends JpaRepository<StageCreationAutorisation, Long> {

    List<StageCreationAutorisation> findByStudentId(Long studentId);

    Optional<StageCreationAutorisation> findByStudentIdAndTypeStage(Long studentId, TypeStage typeStage);

    @Query("SELECT s FROM StageCreationAutorisation s WHERE s.studentId = :studentId " +
           "AND s.typeStage = :typeStage " +
           "AND YEAR(s.createdAt) = :year")
    Optional<StageCreationAutorisation> findByStudentIdAndTypeStageAndYear(
            @Param("studentId") Long studentId,
            @Param("typeStage") TypeStage typeStage,
            @Param("year") Integer year
    );
}
