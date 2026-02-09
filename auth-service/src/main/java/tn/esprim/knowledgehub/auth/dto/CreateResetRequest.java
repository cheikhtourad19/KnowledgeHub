package tn.esprim.knowledgehub.auth.dto;


@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor

public class CreateResetRequest {
    private String email;
}
