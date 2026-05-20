package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.resource.enums.FileType;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long fileId;
    private String fileName;
    private String originalFileName;
    private String title;
    private String description;
    private Long fileSize;
    private String contentType;
    private String storagePath;
    private FileType fileType;
    private Date uploadDate;
    private Long uploadedByUserId;
    private String uploadedByUserName;
    private Integer pageCount;
    private Boolean isPublished;
    private String coverImagePath;
    private String coverOriginalFileName;
    private List<String> keywords;
    private String message;
}
