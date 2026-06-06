package com.swiftcart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket:swiftcart-bucket}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${app.upload.dir:uploads}")
    private String localUploadDir;

    private S3Client s3Client;
    private boolean useLocalFallback = true;

    @PostConstruct
    public void init() {
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            try {
                this.s3Client = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                        .build();
                this.useLocalFallback = false;
                log.info("Initialized AWS S3 Client successfully on bucket: {}", bucketName);
            } catch (Exception e) {
                log.error("Failed to initialize AWS S3 client, falling back to local storage: {}", e.getMessage());
            }
        } else {
            log.info("AWS Credentials not provided. Using local storage fallback directory: {}", localUploadDir);
        }

        if (useLocalFallback) {
            try {
                Files.createDirectories(Paths.get(localUploadDir));
            } catch (IOException e) {
                log.error("Failed to create local upload directory: {}", e.getMessage());
            }
        }
    }

    public String uploadFile(byte[] content, String originalFilename, String contentType) {
        String ext = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot > 0) {
            ext = originalFilename.substring(dot);
        }
        String uniqueFilename = UUID.randomUUID().toString() + ext;

        if (useLocalFallback) {
            File target = new File(localUploadDir, uniqueFilename);
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(content);
                log.info("Saved image locally to: {}", target.getAbsolutePath());
                // Return a mock local URL
                return "http://localhost:8080/uploads/" + uniqueFilename;
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image locally", e);
            }
        } else {
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(uniqueFilename)
                        .contentType(contentType)
                        .acl(ObjectCannedACL.PUBLIC_READ) // Or standard public read configurations
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
                String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, uniqueFilename);
                log.info("Uploaded image to S3: {}", s3Url);
                return s3Url;
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image to S3", e);
            }
        }
    }
}
