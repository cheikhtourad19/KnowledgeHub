package tn.esprim.knowledgehub.resource.dto.response;

import lombok.*;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.TypeStage;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageResponse {

    private Long id;
    private String title;
    private String companyName;
    private TypeStage typeStage;
    private Integer year;
    private Integer durationMonths;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long supervisorId;
    private String supervisorName;
    private String supervisorFeedback;
    private Boolean cloture;
    private Boolean canEdit;
    private Date createdDate;

    // File references (IDs and names for backward compatibility)
    private Long reportFileId;
    private String reportFileName;
    private Long journalFileId;
    private String journalFileName;
    private Long attestationFileId;
    private String attestationFileName;
    private Long conventionFileId;
    private String conventionFileName;

    // Full File objects for frontend
    private File reportFile;
    private File journalFile;
    private File attestationFile;
    private File conventionFile;
}
