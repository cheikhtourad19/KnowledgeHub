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
public class DeleteResponse {

    private Boolean success;

    @JsonProperty("file_id")
    private Integer fileId;

    @JsonProperty("chunks_deleted")
    private Integer chunksDeleted;

    private String message;
}
