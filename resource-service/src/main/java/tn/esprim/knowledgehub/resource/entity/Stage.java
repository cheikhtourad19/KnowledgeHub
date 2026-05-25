package tn.esprim.knowledgehub.resource.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import tn.esprim.knowledgehub.resource.enums.TypeStage;

import java.util.Date;

@Entity
@Table(name = "stage_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String companyName;

    private TypeStage typeStage;

    private Integer year;

    private Integer durationMonths;


    @Column(name = "student_id", nullable = false)
    private Long studentAuthId;

    private String studentName;

    private String studentEmail;

    // supervisorAuthId sera assigné par l'admin plus tard
    @Column(name = "supervisor_id")
    private Long supervisorAuthId;

    private String supervisorName;

    // File IDs - exposés directement pour éviter le lazy loading
    @Column(name = "report_file_id", insertable = false, updatable = false)
    private Long reportFileId;

    @Column(name = "journal_file_id", insertable = false, updatable = false)
    private Long journalFileId;

    @Column(name = "attestation_file_id", insertable = false, updatable = false)
    private Long attestationFileId;

    @Column(name = "convention_file_id", insertable = false, updatable = false)
    private Long conventionFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_file_id")
    private File reportFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_file_id")
    private File journalFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attestation_file_id")
    private File attestationFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convention_file_id")
    private File conventionFile;

    @Builder.Default
    @Column(nullable = false)
    private Boolean cloture = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean canEdit = false;

    @Lob
    private String supervisorFeedback;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;

}
