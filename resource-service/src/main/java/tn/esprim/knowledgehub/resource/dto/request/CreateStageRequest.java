package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStageRequest {

    private String title;
    private String companyName;
    private String description;
    private String subject;
    private Integer year;
    private Integer durationMonths;
    private Long supervisorId;
    private List<String> reportFileKeywords;
    private List<String> journalFileKeywords;
    private List<String> attestationFileKeywords;
}
