package tn.esprim.knowledgehub.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.user.entity.Student;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByAuthId(String authId);
    Optional<Student> findByEmail(String email);
}
