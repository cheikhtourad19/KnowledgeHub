package tn.esprim.knowledgehub.resource.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.dto.response.FileUploadResponse;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.repository.FileRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

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

    @Value("${minio.buckets.cover-pages}")
    private String coverPagesBucket;

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
        return uploadFile(
                multipartFile,
                fileType,
                uploadedByUserId,
                uploadedByUserName,
                bucketType,
                title,
                description,
                published,
                null,
                published
        );
    }

    @Transactional
    public FileUploadResponse uploadFile(
            MultipartFile multipartFile,
            FileType fileType,
            Long uploadedByUserId,
            String uploadedByUserName,
            String bucketType,
            String title,
            String description,
            boolean published,
            MultipartFile coverPage,
            boolean enableCoverForLibrary
    ) {
        try {

            if (multipartFile.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            String originalFileName = multipartFile.getOriginalFilename();
            String contentType = detectContentType(multipartFile);
            long fileSize = multipartFile.getSize();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = originalFileName + UUID.randomUUID() + fileExtension;
            String bucketName = getBucketName(bucketType);

            // 1. Handle cover FIRST: upload provided cover or auto-extract from PDF
            CoverUploadResult coverUploadResult = null;
            if (coverPage != null && !coverPage.isEmpty()) {
                log.info("uploadFile: explicit cover provided, uploading cover first");
                coverUploadResult = uploadProvidedCover(coverPage);
            } else if (enableCoverForLibrary) {
                log.info("uploadFile: no explicit cover, attempting auto-extract from first page");
                coverUploadResult = extractAndUploadPdfFirstPage(multipartFile);
            }

            // 2. Upload file to MinIO
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

            // 3. Count PDF pages
            Integer pageCount = null;
            if (contentType != null && contentType.equals("application/pdf")) {
                pageCount = countPdfPages(multipartFile);
            }

            // 4. Save entity with cover reference
            File file = File.builder()
                    .fileName(uniqueFileName)
                    .originalFileName(originalFileName)
                    .title(title)
                    .description(description)
                    .fileType(fileType)
                    .fileSize(fileSize)
                    .storagePath(bucketName + "/" + uniqueFileName)
                    .coverImagePath(coverUploadResult != null ? coverUploadResult.storagePath() : null)
                    .coverOriginalFileName(coverUploadResult != null ? coverUploadResult.originalFileName() : null)
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
                    .coverImagePath(file.getCoverImagePath())
                    .coverOriginalFileName(file.getCoverOriginalFileName())
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

    public String generateCoverPresignedUrl(Long fileId, int expiryInMinutes) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

            String storagePath = file.getCoverImagePath();
            if (storagePath == null || storagePath.isBlank()) {
                throw new IllegalArgumentException("No cover image available for file: " + fileId);
            }

            String[] pathParts = storagePath.split("/", 2);
            String bucketName = pathParts[0];
            String objectName = pathParts[1];

            String url = minioClientForPresignedUrls.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryInMinutes, TimeUnit.MINUTES)
                            .build()
            );

            log.info("Generated presigned URL for cover of file ID: {} -> {}", fileId, url);
            return url;
        } catch (Exception e) {
            log.error("Error generating presigned URL for cover: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL for cover: " + e.getMessage(), e);
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

            if (file.getCoverImagePath() != null && !file.getCoverImagePath().isBlank()) {
                try {
                    removeObjectByStoragePath(file.getCoverImagePath());
                } catch (Exception e) {
                    log.warn("Failed to delete cover image for file {}: {}", fileId, e.getMessage());
                }
            }


            fileRepository.delete(file);
            if (file.getIsPublished()) {
                try {
                    aiandSearchService.deleteFile(Math.toIntExact(fileId));
                } catch (Exception e) {
                    log.warn("Failed to delete file from AI and Search Service: {}", e.getMessage());
                }
            }
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
        return updatePublicationStatus(fileId, isPublished, null);
    }

    @Transactional
    public File updatePublicationStatus(Long fileId, Boolean isPublished, String role) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

        boolean publishingNow = Boolean.TRUE.equals(isPublished) && !Boolean.TRUE.equals(file.getIsPublished());

        file.setIsPublished(isPublished);

        if (publishingNow && shouldGenerateCoverOnPublish(file, role)) {
            log.info("Publishing file {} - checking cover generation. Current cover: {}", fileId, file.getCoverImagePath());
            ensureCoverPageForPublishedFile(file);
            log.info("Cover generation completed for file {}. New cover: {}", fileId, file.getCoverImagePath());
        }

        if (isPublished) {
            try {
                log.info("Indexing file {} to AI and Search Service", fileId);
                aiandSearchService.indexFile(fileId);
            } catch (Exception e) {
                log.warn("Failed to index file {}: {}", fileId, e.getMessage());
            }
        } else {
            try {
                log.info("Removing file {} from AI and Search Service", fileId);
                aiandSearchService.deleteFile(Math.toIntExact(fileId));
            } catch (Exception e) {
                log.warn("Failed to delete file {} from search service: {}", fileId, e.getMessage());
            }
        }

        log.info("File {} publication status updated to: {}", fileId, isPublished);
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

    /**
     * Attach an uploaded cover image to an existing file record.
     * Accessible from admin controller when admin wants to add a cover after upload.
     */
    @Transactional
    public File attachCoverToFile(Long fileId, MultipartFile coverPage) {
        try {
            File file = getFileById(fileId);
            if (coverPage == null || coverPage.isEmpty()) {
                throw new IllegalArgumentException("coverPage must be provided");
            }

            CoverUploadResult result = uploadProvidedCover(coverPage);
            if (result != null) {
                file.setCoverImagePath(result.storagePath());
                file.setCoverOriginalFileName(result.originalFileName());
                file = fileRepository.save(file);
            }
            return file;
        } catch (Exception e) {
            log.error("Failed to attach cover to file {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException("Failed to attach cover: " + e.getMessage(), e);
        }
    }

    private CoverUploadResult uploadProvidedCover(MultipartFile coverPage) {
        try {
            log.info("uploadProvidedCover: attempting to upload provided cover {} (size={})", coverPage.getOriginalFilename(), coverPage.getSize());
            String coverContentType = detectContentType(coverPage);
            if (coverContentType == null || !coverContentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new IllegalArgumentException("coverPage must be an image (png, jpg, jpeg, webp, etc.)");
            }

            String extension = extensionFromContentType(coverContentType);
            String objectName = "cover-" + UUID.randomUUID() + extension;

            log.info("Uploading provided cover to MinIO bucket: {} - object: {} (contentType: {})", coverPagesBucket, objectName, coverContentType);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(coverPagesBucket)
                            .object(objectName)
                            .stream(coverPage.getInputStream(), coverPage.getSize(), -1)
                            .contentType(coverContentType)
                            .build()
            );

            String storagePath = coverPagesBucket + "/" + objectName;
            log.info("Provided cover successfully saved to MinIO: {}", storagePath);
            return new CoverUploadResult(
                    storagePath,
                    coverPage.getOriginalFilename()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload provided cover page: " + e.getMessage(), e);
        }
    }

    private CoverUploadResult extractAndUploadPdfFirstPage(MultipartFile sourceFile) {
        try {
            String contentType = detectContentType(sourceFile);
            if (!"application/pdf".equals(contentType)) {
                return null;
            }

            byte[] sourceBytes = sourceFile.getBytes();
            return extractAndUploadPdfFirstPage(sourceBytes, sourceFile.getOriginalFilename());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract cover page from uploaded PDF: " + e.getMessage(), e);
        }
    }

    private CoverUploadResult extractAndUploadPdfFirstPage(byte[] pdfBytes, String sourceOriginalName) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.getNumberOfPages() == 0) {
                log.warn("PDF has no pages - cannot generate cover from: {}", sourceOriginalName);
                return null;
            }

            log.info("Rendering first page of PDF to image for: {} (total pages: {})", sourceOriginalName, document.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage firstPageImage = renderer.renderImageWithDPI(0, 150);
            byte[] imageBytes = bufferedImageToPng(firstPageImage);
            String objectName = "cover-" + UUID.randomUUID() + ".png";

            log.info("Uploading cover image to MinIO bucket: {} - object: {} (size: {} bytes)", coverPagesBucket, objectName, imageBytes.length);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(coverPagesBucket)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                            .contentType("image/png")
                            .build()
            );

            String storagePath = coverPagesBucket + "/" + objectName;
            String coverName = buildAutoCoverName(sourceOriginalName);
            log.info("Cover successfully saved to MinIO: {} (original name: {})", storagePath, coverName);
            return new CoverUploadResult(storagePath, coverName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract and upload PDF cover page: " + e.getMessage(), e);
        }
    }

    private byte[] bufferedImageToPng(BufferedImage image) {
        try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode cover image", e);
        }
    }

    private void ensureCoverPageForPublishedFile(File file) {
        if (file.getCoverImagePath() != null && !file.getCoverImagePath().isBlank()) {
            log.info("File {} already has a cover - skipping cover generation: {}", file.getId(), file.getCoverImagePath());
            return;
        }

        log.info("No cover exists for file {} - generating cover from source file", file.getId());
        try {
            String[] pathParts = file.getStoragePath().split("/", 2);
            byte[] fileBytes;

            log.debug("Retrieving source file {} from MinIO: {}/{}", file.getId(), pathParts[0], pathParts[1]);
            try (InputStream sourceFileStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(pathParts[0])
                            .object(pathParts[1])
                            .build())) {
                fileBytes = sourceFileStream.readAllBytes();
            }

            log.info("Extracting cover from PDF for file {}", file.getId());
            CoverUploadResult result = extractAndUploadPdfFirstPage(fileBytes, file.getOriginalFileName());
            if (result != null) {
                log.info("Cover generated and saved to MinIO: {} for file {}", result.storagePath(), file.getId());
                file.setCoverImagePath(result.storagePath());
                file.setCoverOriginalFileName(result.originalFileName());
                log.info("File {} entity updated with cover metadata", file.getId());
            } else {
                log.warn("Cover extraction returned null for file {} - may not be a PDF or PDF is empty", file.getId());
            }
        } catch (Exception e) {
            log.warn("Could not auto-generate cover page for file {}: {}", file.getId(), e.getMessage());
        }
    }

    private boolean shouldGenerateCoverOnPublish(File file, String role) {
        // Generate cover for admin or teacher publishing if no cover exists
        boolean isStaff = "ADMIN".equals(role) || "TEACHER".equals(role);
        boolean noCoverExists = file.getCoverImagePath() == null || file.getCoverImagePath().isBlank();
        return isStaff && noCoverExists;
    }

    private void removeObjectByStoragePath(String storagePath) throws Exception {
        String[] pathParts = storagePath.split("/", 2);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(pathParts[0])
                        .object(pathParts[1])
                        .build()
        );
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/bmp" -> ".bmp";
            default -> ".png";
        };
    }

    private String buildAutoCoverName(String sourceOriginalName) {
        String baseName = sourceOriginalName;
        if (baseName == null || baseName.isBlank()) {
            baseName = "document";
        } else if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }
        return baseName + "-cover.png";
    }

    private record CoverUploadResult(String storagePath, String originalFileName) {}
}
