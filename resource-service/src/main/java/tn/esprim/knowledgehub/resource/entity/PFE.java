package tn.esprim.knowledgehub.resource.entity;

import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pfe_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PFE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;


    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;


    @Column(name = "student_id", nullable = false)
    private Long studentAuthId;

    private String studentName;

    private String studentEmail;

    @Column(name = "supervisor_id", nullable = false)
    private Long supervisorAuthId;
    private String supervisorName;
    @Column(name = "chef_de_jury_id")
    private Long chefDeJuryAuthId;
    private String chefDeJuryName;
    @Column(name = "rapporteur_id")
    private Long rapporteurAuthId;
    private String rapporteurName;
    private String entreprise_name;
    private Date date_debut;
    private Date date_fin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_file_id")
    private File reportFile;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_file_id")
    private File attestationFile;
    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @Lob
    private String supervisorFeedback;

    @Lob
    private String reporteurFeedback;

    private Float plagiarismScore;

    @Builder.Default
    @Column(nullable = false)
    private Boolean cloture = false;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;


}
