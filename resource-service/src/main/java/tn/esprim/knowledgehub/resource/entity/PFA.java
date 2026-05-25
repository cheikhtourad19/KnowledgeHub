package tn.esprim.knowledgehub.resource.entity;

import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pfa_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PFA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;


    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    // User Service references
    @Column(name = "student_id", nullable = false)
    private Long studentAuthId;

    private String studentName;

    private String studentEmail;

    @Column(name = "supervisor_id", nullable = false)
    private Long supervisorAuthId;

    private String supervisorName;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_file_id")
    private File reportFile;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @Lob
    private String supervisorFeedback;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;
}
