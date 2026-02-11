package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubmissionRequest {

    private String title;
    private String description;
    private String subject;
    // Keywords are now managed at the file level, not submission level
}
