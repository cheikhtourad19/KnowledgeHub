package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.resource.entity.Keyword;

public interface KeywordsRepository extends JpaRepository<Long , Keyword> {
}
