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



    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;


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
    @JoinColumn(name = "report_file_id")
    private File reportFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_file_id")
    private File journalFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_file_id")
    private File attestationFile;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @Lob
    private String supervisorFeedback;


    private Float plagiarismScore;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;


}
