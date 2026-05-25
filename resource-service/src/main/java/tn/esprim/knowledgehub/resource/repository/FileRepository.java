package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.FileType;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByFileName(String fileName);

    List<File> findByUploadedByAuthId(Long authId);

    List<File> findByIsPublishedTrue();

    List<File> findByFileType(FileType fileType);

    List<File> findByIsPublishedTrueAndFileType(FileType fileType);


}
