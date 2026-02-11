package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.resource.entity.Keyword;


@Repository
public interface KeywordsRepository extends JpaRepository<Keyword , Long> {
}
