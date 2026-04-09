package tn.esprim.knowledgehub.resource.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final MinioClient minioClient;
    private final MinioClient minioClientForPresignedUrls;
    private final AIandSearchService aiandSearchService;
    private final Tika tika = new Tika();


    // Constructor injection with qualifier for presigned URL client
    public FileService(
            FileRepository fileRepository,
            MinioClient minioClient,
            @Qualifier("minioClientForPresignedUrls") MinioClient minioClientForPresignedUrls,
            AIandSearchService aiandSearchService
    ) {
        this.fileRepository = fileRepository;
        this.minioClient = minioClient;
        this.minioClientForPresignedUrls = minioClientForPresignedUrls;
        this.aiandSearchService = aiandSearchService;
    }

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
     * @param title The title of the file
     * @param description The description of the file
     * @return FileUploadResponse with file details
     */
    @Transactional
    public FileUploadResponse uploadFile(
            MultipartFile multipartFile,
            FileType fileType,
            Long uploadedByUserId,
            String uploadedByUserName,
            String bucketType,
            String title,
            String description,
            boolean published
    ) {
        try {

            if (multipartFile.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }


            String bucketName = getBucketName(bucketType);


            String originalFileName = multipartFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = originalFileName+ UUID.randomUUID() + fileExtension;

            String contentType = detectContentType(multipartFile);
            long fileSize = multipartFile.getSize();
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
            Integer pageCount = null;
            if (contentType != null && contentType.equals("application/pdf")) {
                pageCount = countPdfPages(multipartFile);
            }


            File file = File.builder()
                    .fileName(uniqueFileName)
                    .originalFileName(originalFileName)
                    .title(title)
                    .description(description)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .storagePath(bucketName + "/" + uniqueFileName)
                    .uploadedByUserId(uploadedByUserId)
                    .uploadedByUserName(uploadedByUserName)
                    .pageCount(pageCount)
                    .isPublished(published)
                    .isScanned(false)
                    .build();
            file = fileRepository.save(file);
            log.info("File metadata saved to database with ID: {}", file.getId());
            return FileUploadResponse.builder()
                    .fileId(file.getId())
                    .fileName(uniqueFileName)
                    .originalFileName(originalFileName)
                    .title(title)
                    .description(description)
                    .fileSize(fileSize)
                    .contentType(contentType)
                    .storagePath(file.getStoragePath())
                    .fileType(fileType)
                    .uploadDate(file.getUploadDate())
                    .uploadedByUserId(uploadedByUserId)
                    .uploadedByUserName(uploadedByUserName)
                    .pageCount(pageCount)
                    .isPublished(published)
                    .message("File uploaded successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }





    public String generatePresignedUrl(Long fileId, int expiryInMinutes) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

            String[] pathParts = file.getStoragePath().split("/", 2);
            String bucketName = pathParts[0];
            String objectName = pathParts[1];

            // Use the external MinIO client for presigned URLs (browser-accessible)
            String url = minioClientForPresignedUrls.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryInMinutes, TimeUnit.MINUTES)
                            .build()
            );

            log.info("Generated presigned URL for file ID: {} -> {}", fileId, url);

            return url;

        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    public List<File> getFilesByUserId(Long userId) {
        return fileRepository.findByUploadedByUserId(userId);
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

    public List<File> getAllFiles(){
        return fileRepository.findAll();
    }
    public List<File> getPublishedFiles(){
        return fileRepository.findByIsPublishedTrue();
    }
    public List<File> getFilesByType(FileType fileType) {
        return fileRepository.findByIsPublishedTrueAndFileType(fileType);
    }


    public List<File> getFilesByUploader(Long userId) {
        return fileRepository.findByUploadedByUserId(userId);
    }


    @Transactional
    public File updatePublicationStatus(Long fileId, Boolean isPublished) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

        file.setIsPublished(isPublished);

        if (isPublished) {
            // Index the file when publishing
            try{
                aiandSearchService.indexFile(fileId);
            } catch (Exception e) {
                return fileRepository.save(file);
            }

        } else {
            try {
                aiandSearchService.deleteFile(Math.toIntExact(fileId));

            }catch (Exception e) {
                return fileRepository.save(file);

            }
        }

        return fileRepository.save(file);
    }

    /**
     * Update file title and description
     *
     * @param fileId The ID of the file to update
     * @param title The new title
     * @param description The new description
     * @return Updated file entity
     */
    @Transactional
    public File updateFileMetadata(Long fileId, String title, String description) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

        if (title != null) {
            file.setTitle(title);
        }
        if (description != null) {
            file.setDescription(description);
        }

        log.info("Updated metadata for file ID: {}", fileId);
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



    private Integer countPdfPages(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            log.warn("Could not count PDF pages: {}", e.getMessage());
            return null;
        }
    }


    public File saveFile(File file) {
        return fileRepository.save(file);
    }
}
