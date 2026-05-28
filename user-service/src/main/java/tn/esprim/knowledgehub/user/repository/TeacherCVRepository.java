package tn.esprim.knowledgehub.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.user.entity.TeacherCV;

import java.util.List;
import java.util.Optional;

public interface TeacherCVRepository extends JpaRepository<TeacherCV, Long> {

    List<TeacherCV> findByTeacherTeacherIdOrderByUploadedAtDesc(Long teacherId);

    Optional<TeacherCV> findByTeacherTeacherIdAndActiveTrue(Long teacherId);

    List<TeacherCV> findAllByTeacherTeacherIdAndActiveTrue(Long teacherId);
}
