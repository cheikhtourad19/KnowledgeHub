package tn.esprim.knowledgehub.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.user.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}
