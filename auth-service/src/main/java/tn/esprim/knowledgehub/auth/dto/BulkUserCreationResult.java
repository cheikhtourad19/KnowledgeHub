package tn.esprim.knowledgehub.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUserCreationResult {
    private String email;
    private String nom;
    private String prenom;
    private boolean notified;
}
