package tn.esprim.knowledgehub.resource.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.dto.response.FileDownloadResponse;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.repository.FileRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final MinioClient minioClient;
    private final Tika tika = new Tika();

    @Value("${minio.buckets.documents}")
    private String documentsBucket;

    @Value("${minio.buckets.pfa}")
    private String pfaBucket;
    @Value("${minio.buckets.journal}")
    private String journalBucket;
    @Value("${minio.buckets.attestation}")
    private String attestationBucket;

    @Value("${minio.buckets.pfe}")
    private String pfeBucket;

    @Value("${minio.buckets.stage}")
    private String stageBucket;

    /**
     * Upload a file to MinIO and save metadata to database
     *
     * @param multipartFile The file to upload
     * @param fileType The type of file (REPORT, EDUCATIONAL_MATERIAL, etc.)
     * @param uploadedByUserId The ID of the user uploading the file
     * @param uploadedByUserName The name of the user uploading the file
     * @param bucketType The bucket type (documents, pfa, pfe, stage)
     * @return FileUploadResponse with file details
     */
    @Transactional
    public FileUploadResponse uploadFile(
            MultipartFile multipartFile,
            FileType fileType,
            Long uploadedByUserId,
            String uploadedByUserName,
            String bucketType
    ) {
        try {

            if (multipartFile.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }


            String bucketName = getBucketName(bucketType);

            // Generate unique file name
            String originalFileName = multipartFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Detect content type
            String contentType = detectContentType(multipartFile);

            // Get file size
            long fileSize = multipartFile.getSize();

            // Upload to MinIO
            InputStream inputStream = multipartFile.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(inputStream, fileSize, -1)
                            .contentType(contentType)
                            .build()
            );

            log.info("File uploaded to MinIO: {}/{}", bucketName, uniqueFileName);



            // Count pages if PDF
            Integer pageCount = null;
            if (contentType != null && contentType.equals("application/pdf")) {
                pageCount = countPdfPages(multipartFile);
            }

            // Create File entity
            File file = File.builder()
                    .fileName(uniqueFileName)
                    .originalFileName(originalFileName)
                    .fileSize(fileSize)
                    .storagePath(bucketName + "/" + uniqueFileName)
                    .uploadedByUserId(uploadedByUserId)
                    .uploadedByUserName(uploadedByUserName)
                    .pageCount(pageCount)
                    .isPublished(false)
                    .isScanned(false)
                    .build();

            // Save to database
            file = fileRepository.save(file);

            log.info("File metadata saved to database with ID: {}", file.getId());

            // Build response
            return FileUploadResponse.builder()
                    .fileId(file.getId())
                    .fileName(uniqueFileName)
                    .originalFileName(originalFileName)
                    .fileSize(fileSize)
                    .contentType(contentType)
                    .storagePath(file.getStoragePath())
                    .fileType(fileType)
                    .uploadDate(file.getUploadDate())
                    .uploadedByUserId(uploadedByUserId)
                    .uploadedByUserName(uploadedByUserName)
                    .pageCount(pageCount)
                    .isPublished(false)
                    .keywords(file.getKeywords())
                    .message("File uploaded successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }


    public FileDownloadResponse downloadFile(Long fileId) {
        try {
            // Get file metadata from database
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

            // Extract bucket and object name from storage path
            String[] pathParts = file.getStoragePath().split("/", 2);
            String bucketName = pathParts[0];
            String objectName = pathParts[1];

            // Download from MinIO
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // Read to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            log.info("File downloaded from MinIO: {}", file.getStoragePath());

            return FileDownloadResponse.builder()
                    .fileContent(baos.toByteArray())
                    .fileName(file.getOriginalFileName())
                    .contentType(detectContentTypeFromFileName(file.getFileName()))
                    .fileSize(file.getFileSize())
                    .build();

        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }


    public String generatePresignedUrl(Long fileId, int expiryInMinutes) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

            String[] pathParts = file.getStoragePath().split("/", 2);
            String bucketName = pathParts[0];
            String objectName = pathParts[1];

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryInMinutes, TimeUnit.MINUTES)
                            .build()
            );

            log.info("Generated presigned URL for file ID: {}", fileId);
            return url;

        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }


    @Transactional
    public void deleteFile(Long fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

            String[] pathParts = file.getStoragePath().split("/", 2);
            String bucketName = pathParts[0];
            String objectName = pathParts[1];


            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );


            fileRepository.delete(file);

            log.info("File deleted successfully: {}", file.getStoragePath());

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }


    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));
    }


    public List<File> getFilesByUploader(Long userId) {
        return fileRepository.findByUploadedByUserId(userId);
    }


    @Transactional
    public File updatePublicationStatus(Long fileId, Boolean isPublished) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

        file.setIsPublished(isPublished);
        return fileRepository.save(file);
    }





    private String getBucketName(String bucketType) {
        return switch (bucketType.toLowerCase()) {
            case "documents", "document" -> documentsBucket;
            case "pfa" -> pfaBucket;
            case "pfe" -> pfeBucket;
            case "stage" -> stageBucket;
            case "journal" -> journalBucket;
            case "attestation" -> attestationBucket;
            default -> documentsBucket;
        };
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String detectContentType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream());
        } catch (Exception e) {
            log.warn("Could not detect content type, using default: {}", e.getMessage());
            return file.getContentType();
        }
    }

    private String detectContentTypeFromFileName(String fileName) {
        try {
            return tika.detect(fileName);
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }


    private Integer countPdfPages(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            log.warn("Could not count PDF pages: {}", e.getMessage());
            return null;
        }
    }
}
