package tn.esprim.knowledgehub.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUserCreationResponse {
    private Map<String, List<BulkUserCreationResult>> results;

    // Helper method to build the response with "cree" and "non_cree" keys
    public static BulkUserCreationResponse of(List<BulkUserCreationResult> created, List<BulkUserCreationResult> notCreated) {
        return BulkUserCreationResponse.builder()
                .results(Map.of(
                        "cree", created,
                        "non_cree", notCreated
                ))
                .build();
    }
}
