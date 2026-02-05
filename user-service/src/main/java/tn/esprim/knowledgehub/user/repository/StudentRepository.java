package tn.esprim.knowledgehub.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.user.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
