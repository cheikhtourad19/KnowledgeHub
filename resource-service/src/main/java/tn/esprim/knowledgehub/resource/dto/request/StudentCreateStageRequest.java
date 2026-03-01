package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateStageRequest {

    @NotBlank(message = "Stage title is required")
    private String title;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;
}
