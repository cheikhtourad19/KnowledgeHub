package tn.esprim.knowledgehub.resource.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexResponse {

    private Boolean success;

    private Integer fileId;

    private Integer chunksIndexed;

    private String message;
}
