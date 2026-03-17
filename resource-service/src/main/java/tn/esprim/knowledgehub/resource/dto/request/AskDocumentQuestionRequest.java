package tn.esprim.knowledgehub.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AskDocumentQuestionRequest {

    @NotBlank(message = "Question is required")
    private String question;
}

