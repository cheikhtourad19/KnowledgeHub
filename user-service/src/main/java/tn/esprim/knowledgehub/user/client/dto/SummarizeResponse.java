package tn.esprim.knowledgehub.user.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummarizeResponse {

    private Boolean success;

    @JsonProperty("summary")
    private Map<String, Object> summary;

    private String message;
}
