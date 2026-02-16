package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * Form request DTO for creating PFE demands with file upload
 * Uses @ModelAttribute binding for multipart/form-data
 */
@Data
public class CreateDemandePFEFormRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "Student CIN is required")
    private String studentCin;

    @NotBlank(message = "Student phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String studentPhone;

    @NotBlank(message = "PFE title is required")
    private String pfeTitle;

    @NotBlank(message = "PFE description is required")
    private String pfeDescription;

    @NotBlank(message = "Keywords are required")
    private String motCles;

    @NotBlank(message = "Company domain is required")
    private String entrepriseDomaine;

    @NotBlank(message = "Company name is required")
    private String entrepriseName;

    @NotBlank(message = "Company supervisor name is required")
    private String entrepriseEncadrantName;

    @NotBlank(message = "Company supervisor email is required")
    @Email(message = "Invalid email format")
    private String entrepriseEncadrantEmail;

    @NotBlank(message = "Company supervisor phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String entrepriseEncadrantPhone;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateDebut;

    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateFin;

    /**
     * Convert form request to service request DTO
     */
    public CreateDemandePFERequest toServiceRequest() {
        return CreateDemandePFERequest.builder()
                .studentCin(studentCin)
                .studentPhone(studentPhone)
                .pfeTitle(pfeTitle)
                .pfeDescription(pfeDescription)
                .motCles(motCles)
                .entrepriseDomaine(entrepriseDomaine)
                .entrepriseName(entrepriseName)
                .entrepriseEncadrantName(entrepriseEncadrantName)
                .entrepriseEncadrantEmail(entrepriseEncadrantEmail)
                .entrepriseEncadrantPhone(entrepriseEncadrantPhone)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .build();
    }
}
