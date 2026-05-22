package tn.esprim.knowledgehub.resource.entity;

import jakarta.persistence.*;
import tn.esprim.knowledgehub.resource.enums.TypeUrl;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class AnnonceUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeUrl typeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", nullable = false)
    @JsonBackReference
    private AnnonceStage annonce;

    public void setAnnonce(AnnonceStage annonce) {
        this.annonce = annonce;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TypeUrl getTypeUrl() {
        return typeUrl;
    }

    public void setTypeUrl(TypeUrl typeUrl) {
        this.typeUrl = typeUrl;
    }

    public AnnonceStage getAnnonce() {
        return annonce;
    }
}
