package tn.esprim.knowledgehub.resource.entity;

import tn.esprim.knowledgehub.resource.enums.DocumentCategory;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "teacher_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentCategory category;

    private String subject;


    // Teacher reference
    @Column(nullable = false)
    private Long teacherId;

    private String teacherName;

    private String teacherEmail;



    // File relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Column(name = "keywords")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublished = true;





    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date publishedDate;


}
