package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprim.knowledgehub.resource.enums.DemandStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDemandStatusRequest {

    @NotNull(message = "Status is required")
    private DemandStatus status;

    private String adminFeedback;
}
