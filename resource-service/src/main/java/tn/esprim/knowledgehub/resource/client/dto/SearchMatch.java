package tn.esprim.knowledgehub.resource.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchMatch {

    private Integer fileId;

    private Double score;
}
