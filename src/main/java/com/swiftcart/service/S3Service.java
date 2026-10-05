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
import java.nio.file.Path;
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

    @Value("${app.backend.url:}")
    private String backendBaseUrl;

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
                Path uploadPath = Paths.get(localUploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                log.info("Initialized local upload directory successfully: {}", uploadPath.toAbsolutePath());
            } catch (Exception e) {
                log.warn("Failed to create configured upload directory '{}': {}. Falling back to system temporary directory.", localUploadDir, e.getMessage());
                try {
                    String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
                    Path fallbackPath = Paths.get(tmpDir, "swiftcart-uploads");
                    if (!Files.exists(fallbackPath)) {
                        Files.createDirectories(fallbackPath);
                    }
                    this.localUploadDir = fallbackPath.toString();
                    log.info("Initialized fallback temporary upload directory successfully: {}", fallbackPath.toAbsolutePath());
                } catch (Exception fallbackEx) {
                    log.error("Failed to create fallback temporary upload directory: {}", fallbackEx.getMessage());
                }
            }
        }
    }

    public String uploadFile(byte[] content, String originalFilename, String contentType) {
        String ext = "";
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                String cleanExt = originalFilename.substring(dot).replaceAll("[^a-zA-Z0-9.]", "").toLowerCase();
                if (cleanExt.length() <= 10) {
                    ext = cleanExt;
                }
            }
        }
        String cleanUniqueFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        if (useLocalFallback) {
            File target = new File(localUploadDir, cleanUniqueFilename);
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(content);
                log.info("Saved image locally with id: {}", cleanUniqueFilename);
                
                return getBaseUrl() + "/uploads/" + cleanUniqueFilename;
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image locally", e);
            }
        } else {
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(cleanUniqueFilename)
                        .contentType(contentType)
                        .acl(ObjectCannedACL.PUBLIC_READ) 
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
                log.info("Uploaded image to S3 with id: {}", cleanUniqueFilename);
                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, cleanUniqueFilename);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image to S3", e);
            }
        }
    }

    private String getBaseUrl() {
        if (backendBaseUrl != null && !backendBaseUrl.isBlank()) {
            return backendBaseUrl.replaceAll("/+$", "");
        }
        String renderUrl = System.getenv("RENDER_EXTERNAL_URL");
        if (renderUrl != null && !renderUrl.isBlank()) {
            return renderUrl.replaceAll("/+$", "");
        }
        return "http://localhost:8080";
    }

    public String getLocalUploadDir() {
        return localUploadDir;
    }

    public void setLocalUploadDir(String localUploadDir) {
        this.localUploadDir = localUploadDir;
    }
}
