package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.resource.enums.FileType;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {

    private Long id;
    private String fileName;
    private String originalFileName;
    private FileType fileType;
    private String mimeType;
    private Long fileSize;
    private Date uploadDate;
    private String uploadedByUserName;
    private Integer pageCount;
    private Boolean isScanned;
    private Boolean isMalwareDetected;
    private List<String> keywords; // Keywords for search/indexing
}
