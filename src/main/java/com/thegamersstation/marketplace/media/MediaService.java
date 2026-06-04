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

import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Value("${media.storage.local.base-url:http://localhost:8080/api/v1/uploads}")
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

    private static final int THUMBNAIL_WIDTH = 400;
    private static final double THUMBNAIL_QUALITY = 0.80;

    /**
     * Whitelist of allowed upload folder names to prevent path traversal.
     */
    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "posts", "avatars", "stores", "backgrounds"
    );

    /**
     * Image file signatures (magic bytes) for content verification.
     */
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF87_SIGNATURE = {0x47, 0x49, 0x46, 0x38, 0x37};
    private static final byte[] GIF89_SIGNATURE = {0x47, 0x49, 0x46, 0x38, 0x39};
    private static final byte[] WEBP_RIFF = {0x52, 0x49, 0x46, 0x46};

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
     * Upload image file with automatic thumbnail generation.
     * @param file Multipart file to upload
     * @param folder Folder/path prefix (e.g., "avatars", "posts", "stores")
     * @return Result containing public URLs for both the original and thumbnail image
     */
    public ImageUploadResult uploadImage(MultipartFile file, String folder) {
        validateFolder(folder);
        validateImage(file);

        if ("s3".equalsIgnoreCase(storageProvider) && s3Client != null) {
            try {
                return uploadToS3WithThumbnail(file, folder);
            } catch (BusinessRuleException ex) {
                log.warn("S3 upload failed, falling back to local storage: {}", ex.getMessage());
                String localUrl = uploadToLocal(file, folder);
                return new ImageUploadResult(localUrl, localUrl);
            }
        } else {
            String localUrl = uploadToLocal(file, folder);
            return new ImageUploadResult(localUrl, localUrl);
        }
    }

    /**
     * Upload multiple images with thumbnail generation.
     */
    public List<ImageUploadResult> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(file -> uploadImage(file, folder))
                .toList();
    }

    /**
     * Derive the thumbnail URL from an original image URL using naming convention.
     * e.g. "https://cdn.cloudfront.net/posts/uuid.jpg" → "https://cdn.cloudfront.net/posts/uuid-thumb.jpg"
     * For URLs that don't follow the convention, returns the original URL as fallback.
     */
    public String deriveThumbnailUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return originalUrl;
        }
        int dotIndex = originalUrl.lastIndexOf('.');
        if (dotIndex <= 0) {
            return originalUrl;
        }
        return originalUrl.substring(0, dotIndex) + "-thumb" + originalUrl.substring(dotIndex);
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
     * Validate folder name against whitelist to prevent path traversal.
     */
    private void validateFolder(String folder) {
        if (folder == null || !ALLOWED_FOLDERS.contains(folder.toLowerCase())) {
            throw new BusinessRuleException(
                "Invalid upload folder. Allowed: " + String.join(", ", ALLOWED_FOLDERS),
                "مجلد الرفع غير صحيح"
            );
        }
    }

    /**
     * Validate that a resolved path is within the upload root directory.
     */
    private void validatePathWithinUploadRoot(Path resolvedPath) {
        Path uploadRoot = Paths.get(localUploadDir).normalize().toAbsolutePath();
        Path normalizedPath = resolvedPath.normalize().toAbsolutePath();
        if (!normalizedPath.startsWith(uploadRoot)) {
            log.warn("Path traversal attempt detected: {}", resolvedPath);
            throw new BusinessRuleException(
                "Invalid file path",
                "مسار الملف غير صحيح"
            );
        }
    }

    /**
     * Validate image file: size, content type, extension, and magic bytes.
     */
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException(
                "File is required",
                "الملف مطلوب"
            );
        }

        // Check file size
        long maxSizeBytes = maxSizeMb * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessRuleException(
                    String.format("File size exceeds maximum allowed size of %d MB", maxSizeMb),
                    String.format("حجم الملف يتجاوز الحد الأقصى المسموح وهو %d ميجابايت", maxSizeMb)
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException(
                    "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_IMAGE_TYPES),
                    "نوع الملف غير صحيح. الأنواع المسموحة: " + String.join(", ", ALLOWED_IMAGE_TYPES)
            );
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new BusinessRuleException(
                        "Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS),
                        "امتداد الملف غير صحيح. الامتدادات المسموحة: " + String.join(", ", ALLOWED_EXTENSIONS)
                );
            }
        }

        // Verify file content via magic bytes
        validateImageMagicBytes(file);
    }

    /**
     * Verify file content matches a known image signature (magic bytes).
     */
    private void validateImageMagicBytes(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);
            if (bytesRead < 3) {
                throw new BusinessRuleException(
                    "File is too small to be a valid image",
                    "الملف صغير جداً ليكون صورة صالحة"
                );
            }

            boolean valid = startsWith(header, JPEG_SIGNATURE)
                    || startsWith(header, PNG_SIGNATURE)
                    || startsWith(header, GIF87_SIGNATURE)
                    || startsWith(header, GIF89_SIGNATURE)
                    || startsWith(header, WEBP_RIFF);

            if (!valid) {
                throw new BusinessRuleException(
                    "File content does not match a valid image format",
                    "محتوى الملف لا يطابق صيغة صورة صحيحة"
                );
            }
        } catch (IOException e) {
            throw new BusinessRuleException(
                "Failed to read file content",
                "فشل قراءة محتوى الملف"
            );
        }
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    /**
     * Upload original image + thumbnail to AWS S3.
     */
    private ImageUploadResult uploadToS3WithThumbnail(MultipartFile file, String folder) {
        try {
            // Generate unique key
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String baseName = UUID.randomUUID().toString();
            String s3Key = folder + "/" + baseName + "." + extension;
            String thumbS3Key = folder + "/" + baseName + "-thumb." + extension;

            String contentType = file.getContentType();
            byte[] originalBytes = file.getBytes();

            // 1) Upload original
            PutObjectRequest putOriginal = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) originalBytes.length)
                    .cacheControl("public, max-age=31536000, immutable")
                    .build();
            s3Client.putObject(putOriginal, RequestBody.fromBytes(originalBytes));

            // 2) Generate and upload thumbnail
            byte[] thumbBytes = generateThumbnail(originalBytes, extension);
            PutObjectRequest putThumb = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(thumbS3Key)
                    .contentType(contentType)
                    .contentLength((long) thumbBytes.length)
                    .cacheControl("public, max-age=31536000, immutable")
                    .build();
            s3Client.putObject(putThumb, RequestBody.fromBytes(thumbBytes));

            // Build public URLs
            String originalUrl;
            String thumbnailUrl;
            if (cloudFrontDomain != null && !cloudFrontDomain.isBlank()) {
                originalUrl = "https://" + cloudFrontDomain + "/" + s3Key;
                thumbnailUrl = "https://" + cloudFrontDomain + "/" + thumbS3Key;
            } else {
                String base = String.format("https://%s.s3.%s.amazonaws.com/", s3BucketName, s3Region);
                originalUrl = base + s3Key;
                thumbnailUrl = base + thumbS3Key;
            }

            log.info("Image uploaded to S3: original={}, thumbnail={} ({}KB → {}KB)",
                    originalUrl, thumbnailUrl,
                    originalBytes.length / 1024, thumbBytes.length / 1024);

            return new ImageUploadResult(originalUrl, thumbnailUrl);

        } catch (S3Exception e) {
            log.error("Failed to upload image to S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new BusinessRuleException(
                "Failed to upload image. Please try again.",
                "فشل رفع الصورة. يرجى المحاولة مرة أخرى."
            );
        } catch (IOException e) {
            log.error("Failed to read/process image file", e);
            throw new BusinessRuleException(
                "Failed to upload image. Please try again.",
                "فشل رفع الصورة. يرجى المحاولة مرة أخرى."
            );
        }
    }

    /**
     * Generate a resized thumbnail using Thumbnailator.
     * Resizes to THUMBNAIL_WIDTH maintaining aspect ratio and compresses.
     */
    private byte[] generateThumbnail(byte[] originalBytes, String extension) throws IOException {
        String outputFormat = switch (extension.toLowerCase()) {
            case "png" -> "png";
            case "gif" -> "gif";
            default -> "jpeg";
        };

        ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(originalBytes))
                .width(THUMBNAIL_WIDTH)
                .outputQuality(THUMBNAIL_QUALITY)
                .outputFormat(outputFormat)
                .toOutputStream(thumbOut);
        return thumbOut.toByteArray();
    }

    /**
     * Upload to local storage
     */
    private String uploadToLocal(MultipartFile file, String folder) {
        try {
            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(localUploadDir, folder);
            validatePathWithinUploadRoot(uploadPath);
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "." + extension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            validatePathWithinUploadRoot(filePath);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return public URL
            String publicUrl = localBaseUrl + "/" + folder + "/" + uniqueFilename;
            log.info("Image uploaded to local storage: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to local storage", e);
            throw new BusinessRuleException(
                "Failed to upload image. Please try again.",
                "فشل رفع الصورة. يرجى المحاولة مرة أخرى."
            );
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
            Path filePath = Paths.get(localUploadDir, relativePath).normalize().toAbsolutePath();
            Path uploadRoot = Paths.get(localUploadDir).normalize().toAbsolutePath();

            // Prevent path traversal: ensure resolved path is within upload root
            if (!filePath.startsWith(uploadRoot)) {
                log.warn("Path traversal attempt in delete: {}", imageUrl);
                return;
            }

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

    /**
     * Result record for image upload containing both original and thumbnail URLs.
     */
    public record ImageUploadResult(String url, String thumbnailUrl) {}
}
