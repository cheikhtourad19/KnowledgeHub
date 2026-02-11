package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.Document;
import tn.esprim.knowledgehub.resource.enums.DocumentCategory;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByTeacherId(Long teacherId);

    List<Document> findByCategory(DocumentCategory category);

    List<Document> findByIsPublishedTrue();

    List<Document> findByIsFeaturedTrue();

    List<Document> findBySubject(String subject);

    List<Document> findByTeacherIdAndIsPublishedTrue(Long teacherId, Boolean isPublished);
}
