package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

    @NotNull(message = "La convention de stage est obligatoire")
    private MultipartFile conventionFile;
}
