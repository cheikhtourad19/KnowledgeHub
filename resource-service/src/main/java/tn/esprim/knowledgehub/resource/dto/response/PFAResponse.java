package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PFAResponse {

    private Long id;
    private String title;
    private String description;
    private String subject;
    private Integer year;
    private SubmissionStatus status;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long supervisorId;
    private String supervisorName;
    private FileResponse reportFile;
    private Date submissionDate;
    private Date reviewDate;
    private String supervisorFeedback;
    private Boolean isPublished;
    private Integer viewCount;
    private Integer downloadCount;
    private Date createdDate;
    private Date lastModifiedDate;
}
