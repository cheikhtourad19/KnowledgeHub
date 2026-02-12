package tn.esprim.knowledgehub.resource.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Long studentId;
    private String email;
    private String nom;
    private String prenom;
    private ClasseDTO classe;
}