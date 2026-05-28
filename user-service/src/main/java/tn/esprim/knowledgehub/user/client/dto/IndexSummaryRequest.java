package tn.esprim.knowledgehub.user.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexSummaryRequest {

    private String text;
    private Map<String, Object> metadata;
}
