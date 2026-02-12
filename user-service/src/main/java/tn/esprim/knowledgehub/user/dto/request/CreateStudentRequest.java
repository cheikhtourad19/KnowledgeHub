package tn.esprim.knowledgehub.user.dto.request;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateStudentRequest {

    private String authId;
    private Long StudentId;
    private String email;
    private String prenom;
    private String nom;


}
