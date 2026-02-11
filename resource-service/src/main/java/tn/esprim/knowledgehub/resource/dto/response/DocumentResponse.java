package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.resource.enums.DocumentCategory;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long id;
    private String title;
    private String description;
    private DocumentCategory category;
    private String subject;
    private String targetAudience;
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String teacherDepartment;
    private FileResponse file;
    private List<String> tags; // Document-specific tags
    private Boolean isPublished;
    private Boolean isFeatured;
    private Integer viewCount;
    private Integer downloadCount;
    private Date publishedDate;
    private Date lastModifiedDate;
    private Date featuredDate;
}
