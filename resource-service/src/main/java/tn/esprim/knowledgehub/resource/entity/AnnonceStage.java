package tn.esprim.knowledgehub.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.resource.enums.ModaliteTravail;
import tn.esprim.knowledgehub.resource.enums.TypeAnnonceStage;
import tn.esprim.knowledgehub.resource.enums.TypeStage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnonceStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String nomEntreprise;
    private String ville;
    private String pays;

    private boolean estActive = true;

    private String imageUrl;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "annonce_type_stage", joinColumns = @JoinColumn(name = "annonce_id"))
    @Column(name = "type_stage")
    private Set<TypeAnnonceStage> typesStage = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ModaliteTravail modaliteTravail;

    @OneToMany(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<AnnonceUrl> urls = new ArrayList<>();
}