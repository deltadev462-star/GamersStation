package com.thegamersstation.marketplace.media;

import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
public class MediaService {

    @Value("${media.storage.provider:local}")
    private String storageProvider;

    @Value("${media.storage.local.upload-dir:uploads}")
    private String localUploadDir;

    @Value("${media.storage.local.base-url:http://localhost:8080/uploads}")
    private String localBaseUrl;

    @Value("${media.max-size-mb:10}")
    private int maxSizeMb;

    // AWS S3 Configuration
    @Value("${aws.s3.bucket-name:}")
    private String s3BucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String s3Region;

    @Value("${aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    private final S3Client s3Client;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    public MediaService(
            @Value("${aws.access-key-id:}") String awsAccessKey,
            @Value("${aws.secret-access-key:}") String awsSecretKey,
            @Value("${aws.s3.region:us-east-1}") String region
    ) {
        // Only initialize S3 client if credentials are provided
        if (!awsAccessKey.isBlank() && !awsSecretKey.isBlank()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
            
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
            
            log.info("AWS S3 client initialized successfully for region: {}", region);
        } else {
            this.s3Client = null;
            log.warn("AWS S3 not configured - using local storage");
        }
    }

    /**
     * Upload image file
     * @param file Multipart file to upload
     * @param folder Folder/path prefix (e.g., "avatars", "ads", "stores")
     * @return Public URL of uploaded image (CloudFront URL for S3, local URL otherwise)
     */
    public String uploadImage(MultipartFile file, String folder) {
        validateImage(file);

        if ("s3".equalsIgnoreCase(storageProvider) && s3Client != null) {
            return uploadToS3(file, folder);
        } else {
            return uploadToLocal(file, folder);
        }
    }

    /**
     * Upload multiple images
     */
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(file -> uploadImage(file, folder))
                .toList();
    }

    /**
     * Delete image by URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        if ("s3".equalsIgnoreCase(storageProvider) && s3Client != null) {
            deleteFromS3(imageUrl);
        } else {
            deleteFromLocal(imageUrl);
        }
    }

    /**
     * Delete multiple images
     */
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        imageUrls.forEach(this::deleteImage);
    }

    /**
     * Extract S3 key from CloudFront or S3 URL
     * Example URLs:
     * - CloudFront: https://d111111abcdef8.cloudfront.net/ads/uuid.jpg
     * - S3: https://bucket-name.s3.region.amazonaws.com/ads/uuid.jpg
     */
    public String extractS3Key(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {
            // CloudFront URL pattern
            if (imageUrl.contains("cloudfront.net")) {
                // Extract path after domain: https://domain.cloudfront.net/path/to/file.jpg -> path/to/file.jpg
                String[] parts = imageUrl.split("cloudfront.net/");
                return parts.length > 1 ? parts[1] : null;
            }
            
            // S3 URL pattern
            if (imageUrl.contains("s3") && imageUrl.contains("amazonaws.com")) {
                // Extract path after bucket: https://bucket.s3.region.amazonaws.com/path/to/file.jpg -> path/to/file.jpg
                String[] parts = imageUrl.split("amazonaws.com/");
                return parts.length > 1 ? parts[1] : null;
            }
            
        } catch (Exception e) {
            log.error("Failed to extract S3 key from URL: {}", imageUrl, e);
        }

        return null;
    }

    /**
     * Validate image file
     */
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("File is required");
        }

        // Check file size
        long maxSizeBytes = maxSizeMb * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessRuleException(
                    String.format("File size exceeds maximum allowed size of %d MB", maxSizeMb)
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException(
                    "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_IMAGE_TYPES)
            );
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new BusinessRuleException(
                        "Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS)
                );
            }
        }
    }

    /**
     * Upload to AWS S3
     */
    private String uploadToS3(MultipartFile file, String folder) {
        try {
            // Generate unique key
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "." + extension;
            String s3Key = folder + "/" + uniqueFilename;

            // Prepare S3 metadata
            String contentType = file.getContentType();

            // Upload to S3 with public-read ACL
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            // Return CloudFront URL if configured, otherwise S3 URL
            String publicUrl;
            if (cloudFrontDomain != null && !cloudFrontDomain.isBlank()) {
                publicUrl = "https://" + cloudFrontDomain + "/" + s3Key;
                log.info("Image uploaded to S3 (via CloudFront): {}", publicUrl);
            } else {
                publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                        s3BucketName, s3Region, s3Key);
                log.info("Image uploaded to S3: {}", publicUrl);
            }

            return publicUrl;

        } catch (S3Exception e) {
            log.error("Failed to upload image to S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new BusinessRuleException("Failed to upload image. Please try again.");
        } catch (IOException e) {
            log.error("Failed to read image file", e);
            throw new BusinessRuleException("Failed to upload image. Please try again.");
        }
    }

    /**
     * Upload to local storage
     */
    private String uploadToLocal(MultipartFile file, String folder) {
        try {
            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(localUploadDir, folder);
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "." + extension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return public URL
            String publicUrl = localBaseUrl + "/" + folder + "/" + uniqueFilename;
            log.info("Image uploaded to local storage: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to local storage", e);
            throw new BusinessRuleException("Failed to upload image. Please try again.");
        }
    }

    /**
     * Delete from AWS S3
     */
    private void deleteFromS3(String imageUrl) {
        String s3Key = extractS3Key(imageUrl);
        if (s3Key == null) {
            log.warn("Could not extract S3 key from URL: {}", imageUrl);
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Image deleted from S3: {}", s3Key);

        } catch (S3Exception e) {
            log.error("Failed to delete image from S3: {}", s3Key, e);
        }
    }

    /**
     * Delete from local storage
     */
    private void deleteFromLocal(String imageUrl) {
        try {
            // Extract path from URL
            String relativePath = imageUrl.replace(localBaseUrl + "/", "");
            Path filePath = Paths.get(localUploadDir, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Image deleted from local storage: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete image from local storage: {}", imageUrl, e);
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
