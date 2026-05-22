package tn.esprim.knowledgehub.resource.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
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

    @Value("${minio.buckets.documents}")
    private String documentsBucket;

    @Value("${minio.buckets.pfa}")
    private String pfaBucket;

    @Value("${minio.buckets.pfe}")
    private String pfeBucket;

    @Value("${minio.buckets.stage}")
    private String stageBucket;
    @Value("${minio.buckets.journal}")
    private String journalBucket;
    @Value("${minio.buckets.attestation}")
    private String attestationBucket;

    @Value("${minio.buckets.cover-pages}")
    private String coverPagesBucket;

    @Value("${minio.buckets.annonces}")
    private String annoncesBucket;


    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            createBucketIfNotExists(minioClient, documentsBucket);
            createBucketIfNotExists(minioClient, pfaBucket);
            createBucketIfNotExists(minioClient, pfeBucket);
            createBucketIfNotExists(minioClient, stageBucket);
            createBucketIfNotExists(minioClient, journalBucket);
            createBucketIfNotExists(minioClient, attestationBucket);
            createBucketIfNotExists(minioClient, coverPagesBucket);
            createBucketIfNotExists(minioClient, annoncesBucket);

            // bucket annonces public en lecture
            setBucketPublicReadPolicy(minioClient, annoncesBucket);

            log.info("MinIO client initialized successfully");
            return minioClient;
        } catch (Exception e) {
            log.error("Error initializing MinIO client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }

    private void setBucketPublicReadPolicy(MinioClient minioClient, String bucketName) {
        try {
            String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("Public read policy applied to bucket '{}'", bucketName);
        } catch (Exception e) {
            log.error("Error setting public policy on bucket '{}': {}", bucketName, e.getMessage(), e);
            throw new RuntimeException("Failed to set public policy on bucket: " + bucketName, e);
        }
    }
    /**
     * MinIO client for generating presigned URLs with external endpoint
     * This ensures browsers can access the URLs
     */
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

    private void createBucketIfNotExists(MinioClient minioClient, String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket '{}' created successfully", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket '{}': {}", bucketName, e.getMessage(), e);
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }
}
