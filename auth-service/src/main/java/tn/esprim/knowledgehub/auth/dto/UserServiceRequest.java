package tn.esprim.knowledgehub.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserServiceRequest {
    private String authId;
    private String email;
    private String prenom;
    private String nom;
}
