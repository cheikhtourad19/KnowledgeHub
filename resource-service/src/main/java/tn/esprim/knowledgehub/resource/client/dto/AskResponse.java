package tn.esprim.knowledgehub.resource.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AskResponse {
    private Integer file_id;
    private String query;
    private String answer;
    private String source;
    private Integer characters_in_document;
}

