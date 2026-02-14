package tn.esprim.knowledgehub.resource.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {

    private String query;

    private List<SearchMatch> matches;

    @JsonProperty("total_results")
    private Integer totalResults;
}
