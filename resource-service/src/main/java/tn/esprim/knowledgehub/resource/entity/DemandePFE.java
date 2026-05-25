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

    // Student info - extracted from UserService via X-Auth-Id header
    @Column(name = "student_id", nullable = false)
    private Long studentAuthId;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String studentEmail;

    private String studentClasse;

    // Student info - provided by student in form
    @Column(nullable = false)
    private String studentCin;

    @Column(nullable = false)
    private String studentPhone;

    // PFE details
    @Column(nullable = false)
    private String pfeTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String pfeDescription;

    @Column(nullable = false)
    private String motCles;

    // Company/Entreprise details
    @Column(nullable = false)
    private String entrepriseDomaine;

    @Column(nullable = false)
    private String entrepriseName;

    @Column(nullable = false)
    private String entrepriseEncadrantName;

    @Column(nullable = false)
    private String entrepriseEncadrantEmail;

    @Column(nullable = false)
    private String entrepriseEncadrantPhone;

    // Dates
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dateDebut;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DemandStatus status = DemandStatus.UPLOADED;

    @Column(columnDefinition = "TEXT")
    private String adminFeedback;

    private Long reviewedByAdminAuthId;
    private String reviewedByAdminName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewedAt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @OneToOne
    @JoinColumn(name = "attestation_file_id", referencedColumnName = "id")
    private File attestationFile;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}
