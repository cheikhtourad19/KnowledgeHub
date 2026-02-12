package tn.esprim.knowledgehub.resource.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseDTO {
    private Long classeId;
    private String classeName;
    private List<StudentDTO> students;
}