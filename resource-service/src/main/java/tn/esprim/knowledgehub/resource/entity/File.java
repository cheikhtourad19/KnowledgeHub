package tn.esprim.knowledgehub.resource.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tn.esprim.knowledgehub.resource.enums.FileType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(length = 500)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isPublished = false;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(length = 500)
    private String coverImagePath;

    @Column(length = 255)
    private String coverOriginalFileName;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date uploadDate;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private Long uploadedByAuthId;

    private String uploadedByUserName;

    private Integer pageCount;

    @Column(nullable = false)
    private FileType fileType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isScanned = false;



}
