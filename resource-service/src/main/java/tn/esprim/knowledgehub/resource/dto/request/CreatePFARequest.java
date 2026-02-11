package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePFARequest {

    private String title;
    private String description;
    private String subject;
    private Integer year;
    private Long supervisorId;
    private List<String> fileKeywords; // Keywords for the file
}
