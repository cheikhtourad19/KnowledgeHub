package tn.esprim.knowledgehub.user.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtractTextResponse {

    private Boolean success;

    @JsonProperty("extracted_text")
    private String extractedText;

    private String message;
}
