package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSubmissionRequest {

    private SubmissionStatus status;
    private String feedback;
    private Float plagiarismScore;
}
