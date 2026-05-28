package tn.esprim.knowledgehub.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprim.knowledgehub.user.entity.TeacherCV;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherCVResponse {

    private Long id;
    private Long teacherId;
    private String teacherName;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String extractedText;
    private String aiSummary;
    private LocalDateTime uploadedAt;
    private Boolean active;
    private Integer version;

    public static TeacherCVResponse fromEntity(TeacherCV cv) {
        return TeacherCVResponse.builder()
                .id(cv.getId())
                .teacherId(cv.getTeacher().getTeacherId())
                .teacherName(cv.getTeacher().getNom() + " " + cv.getTeacher().getPrenom())
                .fileName(cv.getFileName())
                .contentType(cv.getContentType())
                .fileSize(cv.getFileSize())
                .extractedText(cv.getExtractedText())
                .aiSummary(cv.getAiSummary())
                .uploadedAt(cv.getUploadedAt())
                .active(cv.getActive())
                .version(cv.getVersion())
                .build();
    }
}
