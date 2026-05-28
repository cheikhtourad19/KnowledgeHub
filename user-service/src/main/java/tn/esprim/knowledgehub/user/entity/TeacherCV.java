package tn.esprim.knowledgehub.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_cv")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherCV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String minioObjectName;

    private String contentType;

    private Long fileSize;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Version
    private Integer version;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
