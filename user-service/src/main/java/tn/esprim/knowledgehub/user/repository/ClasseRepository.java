package tn.esprim.knowledgehub.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.user.entity.Classe;

public interface ClasseRepository extends JpaRepository<Classe,Long> {
}
