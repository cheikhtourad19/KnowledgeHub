package tn.esprim.knowledgehub.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResetResponse {
    private boolean valid;
    private String token;
    private String message;
}
