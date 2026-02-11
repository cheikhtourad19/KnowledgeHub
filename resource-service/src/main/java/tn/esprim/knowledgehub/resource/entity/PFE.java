package tn.esprim.knowledgehub.resource.entity;

import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Lob
    private String description;

    private String subject;

    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    // User Service references
    @Column(nullable = false)
    private Long studentId;

    private String studentName;

    private String studentEmail;

    @Column(nullable = false)
    private Long supervisorId;

    private String supervisorName;

    private Long chefDeJuryId;

    private String chefDeJuryName;

    private Long rapporteurId;

    private String rapporteurName;

    // File relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_file_id", nullable = false)
    private File reportFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_file_id", nullable = false)
    private File journalFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_file_id", nullable = false)
    private File attestationFile;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewDate;

    @Temporal(TemporalType.DATE)
    private Date defenseDate;

    @Lob
    private String supervisorFeedback;

    @Column(precision = 5, scale = 2)
    private Float plagiarismScore;

    @Column(nullable = false)
    private Boolean isPublished = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;
}
