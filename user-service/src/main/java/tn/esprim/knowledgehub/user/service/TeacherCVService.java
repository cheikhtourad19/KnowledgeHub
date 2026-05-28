package tn.esprim.knowledgehub.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.user.client.AiServiceClient;
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.entity.TeacherCV;
import tn.esprim.knowledgehub.user.repository.TeacherCVRepository;
import tn.esprim.knowledgehub.user.repository.TeacherRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TeacherCVService {

    private final TeacherCVRepository teacherCVRepository;
    private final TeacherRepository teacherRepository;
    private final MinioClient minioClient;
    private final MinioClient minioClientForPresignedUrls;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public TeacherCVService(TeacherCVRepository teacherCVRepository,
                            TeacherRepository teacherRepository,
                            @Qualifier("minioClient") MinioClient minioClient,
                            @Qualifier("minioClientForPresignedUrls") MinioClient minioClientForPresignedUrls,
                            AiServiceClient aiServiceClient,
                            ObjectMapper objectMapper) {
        this.teacherCVRepository = teacherCVRepository;
        this.teacherRepository = teacherRepository;
        this.minioClient = minioClient;
        this.minioClientForPresignedUrls = minioClientForPresignedUrls;
        this.aiServiceClient = aiServiceClient;
        this.objectMapper = objectMapper;
    }

    @Value("${minio.buckets.teacher-cv}")
    private String teacherCvBucket;

    @Transactional(rollbackFor = Exception.class)
    public TeacherCV uploadCV(MultipartFile file, String authId) {
        Teacher teacher = teacherRepository.findByAuthId(authId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with authId: " + authId));

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = "teacher-cv/" + teacher.getTeacherId() + "/" + UUID.randomUUID() + extension;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(teacherCvBucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("CV uploaded to MinIO: bucket={}, object={}", teacherCvBucket, objectName);
        } catch (Exception e) {
            log.error("Failed to upload CV to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload CV file to storage", e);
        }

        // Delete old summary embeddings from ChromaDB before removing old CVs
        try {
            aiServiceClient.deleteSummaryByTeacher(teacher.getTeacherId());
            log.info("Deleted old summary embeddings for teacherId={}", teacher.getTeacherId());
        } catch (Exception e) {
            log.warn("Failed to delete old summary embeddings for teacherId={}: {}", teacher.getTeacherId(), e.getMessage());
        }

        // Deactivate any existing active CVs so only one stays active per teacher
        List<TeacherCV> activeCvs = teacherCVRepository.findAllByTeacherTeacherIdAndActiveTrue(teacher.getTeacherId());
        for (TeacherCV activeCv : activeCvs) {
            
            teacherCVRepository.delete(activeCv);
        }

        TeacherCV cv = TeacherCV.builder()
                .teacher(teacher)
                .fileName(originalFileName != null ? originalFileName : "cv.pdf")
                .minioObjectName(objectName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .active(true)
                .build();

        try {
            cv = teacherCVRepository.save(cv);
        } catch (Exception e) {
            cleanupMinioFile(objectName);
            throw new RuntimeException("Failed to save CV record", e);
        }

        try {
            String extractedText = extractTextFromPdf(file);
            cv.setExtractedText(extractedText);

            String summary = summarizeText(extractedText);
            cv.setAiSummary(summary);

            indexCvSummary(summary, teacher.getTeacherId(), teacher.getNom(), teacher.getPrenom());

            teacherCVRepository.save(cv);
            log.info("CV processed successfully for teacherId={}", teacher.getTeacherId());
        } catch (Exception e) {
            log.error("AI processing failed for CV id={}, rolling back: {}", cv.getId(), e.getMessage());
            teacherCVRepository.delete(cv);
            cleanupMinioFile(objectName);
            throw new RuntimeException("AI processing failed, CV upload rolled back: " + e.getMessage(), e);
        }

        return cv;
    }

    private String extractTextFromPdf(MultipartFile file) {
        try {
            var response = aiServiceClient.extractPdfText(file);
            if (response.getSuccess() != null && response.getSuccess()) {
                return response.getExtractedText();
            }
            throw new RuntimeException("AI text extraction failed: " + response.getMessage());
        } catch (Exception e) {
            log.error("Error calling AI extract-text service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract text from PDF via AI service", e);
        }
    }

    private String summarizeText(String text) {
        try {
            var request = tn.esprim.knowledgehub.user.client.dto.SummarizeRequest.builder()
                    .text(text)
                    .model("gemini")
                    .build();
            var response = aiServiceClient.summarizeText(request);
            if (response.getSuccess() != null && response.getSuccess()) {
                Map<String, Object> summaryMap = response.getSummary();
                return objectMapper.writeValueAsString(summaryMap);
            }
            throw new RuntimeException("AI summarization failed: " + response.getMessage());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize summary map to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize CV summary", e);
        } catch (Exception e) {
            log.error("Error calling AI summarize service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to summarize CV via AI service", e);
        }
    }

    @Transactional(readOnly = true)
    public List<TeacherCV> getTeacherCVs(String authId) {
        Teacher teacher = teacherRepository.findByAuthId(authId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with authId: " + authId));
        return teacherCVRepository.findByTeacherTeacherIdOrderByUploadedAtDesc(teacher.getTeacherId());
    }

    @Transactional(readOnly = true)
    public TeacherCV getActiveCV(String authId) {
        Teacher teacher = teacherRepository.findByAuthId(authId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with authId: " + authId));
        return teacherCVRepository.findByTeacherTeacherIdAndActiveTrue(teacher.getTeacherId())
                .orElseThrow(() -> new RuntimeException("No active CV found for teacher"));
    }

    @Transactional(readOnly = true)
    public TeacherCV getCVById(Long cvId) {
        return teacherCVRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV not found with id: " + cvId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCV(Long cvId) {
        TeacherCV cv = teacherCVRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV not found with id: " + cvId));

        try {
            aiServiceClient.deleteSummaryByTeacher(cv.getTeacher().getTeacherId());
            log.info("Deleted summary embeddings for teacherId={}", cv.getTeacher().getTeacherId());
        } catch (Exception e) {
            log.warn("Failed to delete summary embeddings: {}", e.getMessage());
        }

        cleanupMinioFile(cv.getMinioObjectName());
        teacherCVRepository.delete(cv);
        log.info("CV deleted: id={}, object={}", cvId, cv.getMinioObjectName());
    }

    private void indexCvSummary(String summaryJson, Long teacherId, String nom, String prenom) {
        try {
            var summaryMap = objectMapper.readValue(summaryJson, Map.class);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("teacher_id", teacherId);
            metadata.put("teacher_name", nom + " " + prenom);
            metadata.put("document_type", "cv_summary");

            var request = tn.esprim.knowledgehub.user.client.dto.IndexSummaryRequest.builder()
                    .text(summaryJson)
                    .metadata(metadata)
                    .build();

            var response = aiServiceClient.indexSummary(request);
            log.info("CV summary indexed for teacherId={}: {} chunks", teacherId, response.getChunksIndexed());
        } catch (Exception e) {
            log.error("Failed to index CV summary for teacherId={}: {}", teacherId, e.getMessage());
            throw new RuntimeException("Failed to index CV summary in vector DB", e);
        }
    }

    public String generatePresignedUrl(Long cvId, int expiryInMinutes) {
        TeacherCV cv = teacherCVRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV not found with id: " + cvId));
        try {
            String url = minioClientForPresignedUrls.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(teacherCvBucket)
                            .object(cv.getMinioObjectName())
                            .expiry(expiryInMinutes, TimeUnit.MINUTES)
                            .build()
            );
            log.info("Generated presigned URL for CV id={}", cvId);
            return url;
        } catch (Exception e) {
            log.error("Error generating presigned URL for CV id={}: {}", cvId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    private void cleanupMinioFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(teacherCvBucket)
                            .object(objectName)
                            .build()
            );
            log.info("Cleaned up MinIO file: {}", objectName);
        } catch (Exception e) {
            log.warn("Failed to clean up MinIO file {}: {}", objectName, e.getMessage());
        }
    }
}
