package tn.esprim.knowledgehub.resource.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePFERequest {

    @NotBlank(message = "PFE title is required")
    private String title;

    @NotNull(message = "Academic year is required")
    private Integer year;

    @NotNull(message = "Student auth ID is required")
    private Long studentAuthId;
    private String studentName;
    private String studentEmail;

    private String entrepriseName;
    private Date dateDebut;
    private Date dateFin;

    @NotNull(message = "Supervisor auth ID is required")
    private Long supervisorAuthId;
    private String supervisorName;


    private Long chefDeJuryAuthId;
    private String chefDeJuryName;

    private Long rapporteurAuthId;
    private String rapporteurName;
}