package tn.esprim.knowledgehub.resource.client.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AskRequest {

    @NotNull(message = "File ID is required")
    @Min(value = 1, message = "File ID must be greater than 0")
    private Integer file_id;

    @NotBlank(message = "Bucket name is required")
    private String bucket;

    @NotBlank(message = "Object name is required")
    private String object_name;

    @NotBlank(message = "Query is required")
    private String query;
}

