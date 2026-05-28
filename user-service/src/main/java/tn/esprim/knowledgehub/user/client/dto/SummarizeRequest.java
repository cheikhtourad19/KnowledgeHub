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
public class SummarizeRequest {

    @JsonProperty("text")
    private String text;

    @JsonProperty("model")
    private String model;
}
