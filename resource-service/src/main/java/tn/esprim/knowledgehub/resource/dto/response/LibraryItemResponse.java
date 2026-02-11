package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryItemResponse {

    private Long id;
    private String title;
    private String description;
    private String type; // PFA, PFE, STAGE, DOCUMENT
    private String subject;
    private Integer year;
    private String authorName;
    private String authorEmail;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private List<String> keywords;
    private Integer viewCount;
    private Integer downloadCount;
    private Date publishedDate;
}
