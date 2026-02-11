package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.resource.enums.DocumentCategory;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDocumentRequest {

    private String title;
    private String description;
    private DocumentCategory category;
    private String subject;
    private String targetAudience;
    private List<String> fileKeywords; // Keywords for the file
    private List<String> tags; // Document-specific tags
    private Boolean isPublished;
}
