package tn.esprim.knowledgehub.resource.client.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexRequest {

    @NotNull(message = "File ID is required")
    @Min(value = 1, message = "File ID must be greater than 0")
    private Integer fileId;

    @NotBlank(message = "Bucket name is required")
    private String bucket;

    @NotBlank(message = "Object name is required")
    private String objectName;
}
