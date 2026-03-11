package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.Stage;

import java.util.List;
import java.util.Optional;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

    List<Stage> findByStudentId(Long studentId);

    @Query("SELECT s FROM Stage s WHERE s.reportFile.id = :fileId OR s.journalFile.id = :fileId OR s.attestationFile.id = :fileId")
    Optional<Stage> findByAnyFileId(@Param("fileId") Long fileId);

    List<Stage> findBySupervisorId(Long supervisorId);

    List<Stage> findByYear(Integer year);

    /**
     * Fetch Stage with all File associations eagerly loaded
     */
    @Query("SELECT s FROM Stage s " +
           "LEFT JOIN FETCH s.reportFile " +
           "LEFT JOIN FETCH s.journalFile " +
           "LEFT JOIN FETCH s.attestationFile " +
           "WHERE s.id = :id")
    Optional<Stage> findByIdWithFiles(@Param("id") Long id);

    /**
     * Fetch all Stages with all File associations eagerly loaded
     */
    @Query("SELECT DISTINCT s FROM Stage s " +
           "LEFT JOIN FETCH s.reportFile " +
           "LEFT JOIN FETCH s.journalFile " +
           "LEFT JOIN FETCH s.attestationFile")
    List<Stage> findAllWithFiles();

    /**
     * Fetch Stages by supervisor with all File associations eagerly loaded
     */
    @Query("SELECT s FROM Stage s " +
           "LEFT JOIN FETCH s.reportFile " +
           "LEFT JOIN FETCH s.journalFile " +
           "LEFT JOIN FETCH s.attestationFile " +
           "WHERE s.supervisorId = :supervisorId")
    List<Stage> findBySupervisorIdWithFiles(@Param("supervisorId") Long supervisorId);

    /**
     * Fetch Stages by student with all File associations eagerly loaded
     */
    @Query("SELECT s FROM Stage s " +
           "LEFT JOIN FETCH s.reportFile " +
           "LEFT JOIN FETCH s.journalFile " +
           "LEFT JOIN FETCH s.attestationFile " +
           "WHERE s.studentId = :studentId")
    List<Stage> findByStudentIdWithFiles(@Param("studentId") Long studentId);


}
