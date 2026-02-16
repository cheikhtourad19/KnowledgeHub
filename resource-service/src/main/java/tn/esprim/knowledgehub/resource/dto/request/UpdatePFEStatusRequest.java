package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprim.knowledgehub.resource.enums.SubmissionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePFEStatusRequest {

    @NotNull(message = "Status is required")
    private SubmissionStatus status;

    private String supervisorFeedback;
}
