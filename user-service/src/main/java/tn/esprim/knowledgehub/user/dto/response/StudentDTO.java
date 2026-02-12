package tn.esprim.knowledgehub.user.dto.response;

import lombok.*;
import tn.esprim.knowledgehub.user.entity.Student;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Long studentId;
    private String authId;
    private String email;
    private String nom;
    private String prenom;
    private ClasseBasicDTO classe;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClasseBasicDTO {
        private Long classeId;
        private String classeName;
    }

    public static StudentDTO fromEntity(Student student) {
        StudentDTO dto = StudentDTO.builder()
                .studentId(student.getStudentId())
                .authId(student.getAuthId())
                .email(student.getEmail())
                .nom(student.getNom())
                .prenom(student.getPrenom())
                .build();

        if (student.getClasse() != null) {
            dto.setClasse(ClasseBasicDTO.builder()
                    .classeId(student.getClasse().getClasseId())
                    .classeName(student.getClasse().getClasseName())
                    .build());
        }

        return dto;
    }
}