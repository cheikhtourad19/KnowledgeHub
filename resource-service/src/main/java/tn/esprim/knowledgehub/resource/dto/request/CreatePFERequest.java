package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePFERequest {

    private String title;
    private String description;
    private String subject;
    private Integer year;
    private Long supervisorId;
    private Long chefDeJuryId;
    private Long rapporteurId;
    private Date defenseDate;
    private List<String> reportFileKeywords; // Keywords for report file
    private List<String> journalFileKeywords; // Keywords for journal file
    private List<String> attestationFileKeywords; // Keywords for attestation file
}
