package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDemandePFERequest {

    @NotBlank(message = "Student CIN is required")
    private String studentCin;

    @NotBlank(message = "Student phone is required")
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
    private String entrepriseEncadrantEmail;

    @NotBlank(message = "Company supervisor phone is required")
    private String entrepriseEncadrantPhone;

    @NotNull(message = "Start date is required")
    private Date dateDebut;

    @NotNull(message = "End date is required")
    private Date dateFin;
}
