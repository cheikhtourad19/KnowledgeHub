package tn.esprim.knowledgehub.user.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.external-endpoint}")
    private String externalEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.buckets.teacher-cv}")
    private String teacherCvBucket;

    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            createBucketIfNotExists(client, teacherCvBucket);
            log.info("MinIO client initialized successfully for user-service");
            return client;
        } catch (Exception e) {
            log.error("Error initializing MinIO client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }

    @Bean(name = "minioClientForPresignedUrls")
    public MinioClient minioClientForPresignedUrls() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(externalEndpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            log.info("MinIO presigned URL client initialized with external endpoint: {}", externalEndpoint);
            return client;
        } catch (Exception e) {
            log.error("Error initializing MinIO presigned URL client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MinIO presigned URL client", e);
        }
    }

    private void createBucketIfNotExists(MinioClient client, String bucketName) {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket '{}': {}", bucketName, e.getMessage(), e);
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }
}
