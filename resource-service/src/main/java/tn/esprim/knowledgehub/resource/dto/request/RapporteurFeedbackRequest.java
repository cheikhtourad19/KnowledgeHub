
package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapporteurFeedbackRequest {

    @NotBlank(message = "Feedback is required")
    private String feedback;
}
