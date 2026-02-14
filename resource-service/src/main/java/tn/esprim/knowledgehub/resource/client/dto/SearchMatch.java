package tn.esprim.knowledgehub.resource.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchMatch {

    @JsonProperty("file_id")
    private Integer fileId;

    private Double score;
}
