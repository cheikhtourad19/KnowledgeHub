package tn.esprim.knowledgehub.resource.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprim.knowledgehub.resource.enums.DemandStatus;

import java.util.Date;

@Entity
@Table(name = "demande_pfe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandePFE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String student_classe;
    private String student_identifiant;
    private String student_cin;
    private String pfeTitle;
    private String pfeDescription;
    private String mot_cles;
    private String entreprise_domaine;
    private String entreprise_name;
    private String entreprise_encadrant_name;
    private String entreprise_encadrant_email;
    private String entreprise_encadrant_phone;
    private Date date_debut;
    private Date date_fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DemandStatus status = DemandStatus.UPLOADED;

    @Column(columnDefinition = "TEXT")
    private String adminFeedback;

    private Long reviewedByAdminId;
    private String reviewedByAdminName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewedAt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}
