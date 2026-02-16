package tn.esprim.knowledgehub.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprim.knowledgehub.user.entity.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByAuthId(String authId);
    Optional<Student> findByEmail(String email);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.classe WHERE s.studentId = :id")
    Optional<Student> findByIdWithClasse(@Param("id") Long id);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.classe")
    List<Student> findAllWithClasse();
}
