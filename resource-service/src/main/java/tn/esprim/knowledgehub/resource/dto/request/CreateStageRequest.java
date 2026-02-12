package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStageRequest {

    @NotBlank(message = "Stage title is required")
    private String title;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Academic year is required")
    private Integer year;

    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;

    @NotNull(message = "Student ID is required")
    private Long studentId;
    private String studentName;
    private String studentEmail;

    @NotNull(message = "Supervisor ID is required")
    private Long supervisorId;
    private String supervisorName;
}