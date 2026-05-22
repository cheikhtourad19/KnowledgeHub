package tn.esprim.knowledgehub.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.resource.entity.AnnonceStage;
import tn.esprim.knowledgehub.resource.enums.ModaliteTravail;
import tn.esprim.knowledgehub.resource.enums.TypeAnnonceStage;

import java.util.List;
import java.util.Set;

public interface AnnonceStageRepository extends JpaRepository<AnnonceStage, Long> {

    List<AnnonceStage> findByTypesStageContaining(TypeAnnonceStage type);

    List<AnnonceStage> findByModaliteTravail(ModaliteTravail modalite);

    List<AnnonceStage> findByEstActiveTrue();

    List<AnnonceStage> findByVilleIgnoreCase(String ville);

    List<AnnonceStage> findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titre, String description);
}
