package com.swiftcart.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class S3ServiceTest {

    private S3Service s3Service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service();
    }

    @Test
    void testInit_WithValidDirectory_CreatesDirectorySuccessfully() {
        Path targetUploads = tempDir.resolve("test-uploads");
        ReflectionTestUtils.setField(s3Service, "localUploadDir", targetUploads.toString());
        ReflectionTestUtils.setField(s3Service, "bucketName", "swiftcart-bucket");
        ReflectionTestUtils.setField(s3Service, "region", "us-east-1");

        s3Service.init();

        assertTrue(Files.exists(targetUploads));
        assertEquals(targetUploads.toString(), s3Service.getLocalUploadDir());
    }

    @Test
    void testInit_WithUnwritableDirectory_FallsBackToTempDirectory() throws IOException {
        // Create a regular file so trying to createDirectories inside it throws an exception
        File blockingFile = tempDir.resolve("not-a-directory").toFile();
        assertTrue(blockingFile.createNewFile());

        String invalidSubdir = blockingFile.getAbsolutePath() + "/forbidden-uploads";
        ReflectionTestUtils.setField(s3Service, "localUploadDir", invalidSubdir);

        s3Service.init();

        assertNotEquals(invalidSubdir, s3Service.getLocalUploadDir());
        assertTrue(s3Service.getLocalUploadDir().contains("swiftcart-uploads"));
        assertTrue(Files.exists(Path.of(s3Service.getLocalUploadDir())));
    }

    @Test
    void testUploadFile_LocalFallback_SavesFileAndReturnsUrl() {
        Path targetUploads = tempDir.resolve("uploads");
        ReflectionTestUtils.setField(s3Service, "localUploadDir", targetUploads.toString());
        ReflectionTestUtils.setField(s3Service, "backendBaseUrl", "http://localhost:8080");

        s3Service.init();

        byte[] sampleContent = "test image binary data".getBytes();
        String fileUrl = s3Service.uploadFile(sampleContent, "product-photo.png", "image/png");

        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("http://localhost:8080/uploads/"));
        assertTrue(fileUrl.endsWith(".png"));

        // Verify file exists on disk
        String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        File savedFile = new File(targetUploads.toFile(), filename);
        assertTrue(savedFile.exists());
        assertEquals(sampleContent.length, savedFile.length());
    }
}
