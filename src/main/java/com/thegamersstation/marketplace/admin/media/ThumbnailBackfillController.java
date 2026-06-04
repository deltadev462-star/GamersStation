package com.thegamersstation.marketplace.admin.media;

import com.thegamersstation.marketplace.media.MediaService;
import com.thegamersstation.marketplace.post.PostImage;
import com.thegamersstation.marketplace.post.PostImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * One-time admin endpoint to generate thumbnails for existing S3 images.
 * Call once via: POST /api/v1/admin/media/backfill-thumbnails
 * Can be safely re-run — it skips images that already have thumbnails in S3.
 */
@Slf4j
@RestController
@RequestMapping("/admin/media")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ThumbnailBackfillController {

    private final PostImageRepository postImageRepository;
    private final MediaService mediaService;

    @Value("${aws.s3.bucket-name:}")
    private String s3BucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String s3Region;

    @Value("${aws.access-key-id:}")
    private String awsAccessKey;

    @Value("${aws.secret-access-key:}")
    private String awsSecretKey;

    private static final int THUMBNAIL_WIDTH = 400;
    private static final double THUMBNAIL_QUALITY = 0.80;

    @PostMapping("/backfill-thumbnails")
    public ResponseEntity<Map<String, Object>> backfillThumbnails() {
        if (awsAccessKey.isBlank() || awsSecretKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "AWS credentials not configured"
            ));
        }

        S3Client s3 = S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();

        List<PostImage> allImages = postImageRepository.findAll();
        int processed = 0;
        int skipped = 0;
        int failed = 0;

        log.info("Starting thumbnail backfill for {} images", allImages.size());

        for (PostImage image : allImages) {
            String originalUrl = image.getUrl();
            String expectedThumbUrl = mediaService.deriveThumbnailUrl(originalUrl);

            // Skip if thumbnail URL is already different from original (already backfilled)
            String s3Key = mediaService.extractS3Key(originalUrl);
            if (s3Key == null) {
                log.warn("Skipping non-S3 image: {}", originalUrl);
                skipped++;
                continue;
            }

            String thumbS3Key = mediaService.extractS3Key(expectedThumbUrl);
            if (thumbS3Key == null) {
                skipped++;
                continue;
            }

            // Check if thumbnail already exists in S3
            try {
                s3.headObject(HeadObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(thumbS3Key)
                        .build());
                // Thumbnail exists — update DB if needed and skip
                if (image.getThumbnailUrl().equals(image.getUrl())) {
                    image.setThumbnailUrl(expectedThumbUrl);
                    postImageRepository.save(image);
                }
                skipped++;
                continue;
            } catch (NoSuchKeyException e) {
                // Thumbnail doesn't exist — generate it
            }

            try {
                // Download original from S3
                byte[] originalBytes = s3.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(s3Key)
                        .build()).asByteArray();

                // Generate thumbnail
                String extension = s3Key.substring(s3Key.lastIndexOf('.') + 1);
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
                byte[] thumbBytes = thumbOut.toByteArray();

                // Upload thumbnail to S3
                String contentType = switch (outputFormat) {
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    default -> "image/jpeg";
                };

                s3.putObject(PutObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(thumbS3Key)
                        .contentType(contentType)
                        .contentLength((long) thumbBytes.length)
                        .cacheControl("public, max-age=31536000, immutable")
                        .build(), RequestBody.fromBytes(thumbBytes));

                // Update DB
                image.setThumbnailUrl(expectedThumbUrl);
                postImageRepository.save(image);

                processed++;
                log.info("Backfilled thumbnail: {} ({}KB → {}KB)",
                        thumbS3Key, originalBytes.length / 1024, thumbBytes.length / 1024);

            } catch (Exception e) {
                failed++;
                log.error("Failed to backfill thumbnail for {}: {}", s3Key, e.getMessage());
            }
        }

        log.info("Thumbnail backfill complete: processed={}, skipped={}, failed={}",
                processed, skipped, failed);

        return ResponseEntity.ok(Map.of(
                "total", allImages.size(),
                "processed", processed,
                "skipped", skipped,
                "failed", failed
        ));
    }
}
