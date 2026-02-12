package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor      // ✅ Add this
@AllArgsConstructor     // ✅ Add this
public class CreatePFARequest {

    @NotBlank(message = "Project title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @NotNull(message = "Academic year is required")
    @Min(value = 2000, message = "Year must be after 2000")
    @Max(value = 2100, message = "Year must be before 2100")
    private Integer year;

    @NotNull(message = "Student ID is required")
    @Min(value = 1, message = "Student ID must be positive")
    private Long studentId;

    private String studentName;
    private String studentEmail;

    @NotNull(message = "Supervisor ID is required")
    @Min(value = 1, message = "Supervisor ID must be positive")
    private Long supervisorId;

    private String supervisorName;
}