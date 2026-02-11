package tn.esprim.knowledgehub.resource.entity;

import tn.esprim.knowledgehub.resource.enums.FileType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private Boolean isPublished = false;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 500)
    private String storagePath;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date uploadDate;

    @Column(nullable = false)
    private Long uploadedByUserId;

    private String uploadedByUserName;

    private Integer pageCount;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String extractedText;


    @Column(name = "keywords")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isScanned = false;



}
