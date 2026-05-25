
package tn.esprim.knowledgehub.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.user.entity.Teacher;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDTO {
    private String authId;
    private String email;
    private String nom;
    private String prenom;

    public static TeacherDTO fromEntity(Teacher teacher) {
        return TeacherDTO.builder()
                .authId(teacher.getAuthId())
                .email(teacher.getEmail())
                .nom(teacher.getNom())
                .prenom(teacher.getPrenom())
                .build();
    }
}