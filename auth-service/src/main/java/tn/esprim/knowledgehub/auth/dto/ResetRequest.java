package tn.esprim.knowledgehub.auth.dto;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetRequest {
    private String newPassword;
    private String confirmPassword;
}

