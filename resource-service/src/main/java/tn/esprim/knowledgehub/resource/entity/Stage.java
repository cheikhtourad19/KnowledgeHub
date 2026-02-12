package tn.esprim.knowledgehub.resource.entity;

import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "stage_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String companyName;


    private Integer year;

    private Integer durationMonths;


    @Column(nullable = false)
    private Long studentId;

    private String studentName;

    private String studentEmail;

    @Column(nullable = false)
    private Long supervisorId;

    private String supervisorName;

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

    @Lob
    private String supervisorFeedback;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;

}
