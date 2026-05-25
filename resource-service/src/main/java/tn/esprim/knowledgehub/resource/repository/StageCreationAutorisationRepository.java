package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprim.knowledgehub.resource.entity.StageCreationAutorisation;
import tn.esprim.knowledgehub.resource.enums.TypeStage;

import java.util.List;
import java.util.Optional;

public interface StageCreationAutorisationRepository extends JpaRepository<StageCreationAutorisation, Long> {

    List<StageCreationAutorisation> findByStudentAuthId(Long studentAuthId);

    Optional<StageCreationAutorisation> findByStudentAuthIdAndTypeStage(Long studentAuthId, TypeStage typeStage);

    @Query("SELECT s FROM StageCreationAutorisation s WHERE s.studentAuthId = :studentAuthId " +
           "AND s.typeStage = :typeStage " +
           "AND YEAR(s.createdAt) = :year")
    Optional<StageCreationAutorisation> findByStudentAuthIdAndTypeStageAndYear(
            @Param("studentAuthId") Long studentAuthId,
            @Param("typeStage") TypeStage typeStage,
            @Param("year") Integer year
    );
}
