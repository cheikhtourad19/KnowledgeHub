package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadResponse {

    private byte[] fileContent;
    private String fileName;
    private String contentType;
    private Long fileSize;
}
