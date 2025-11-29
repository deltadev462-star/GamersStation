# AWS S3 + CloudFront Setup Guide

## Overview
The Gamers Station Marketplace API uses AWS S3 for media storage and CloudFront for fast content delivery.

## Architecture

```
User Upload → Spring Boot API → AWS S3 → CloudFront CDN → End Users
```

- **AWS S3**: Object storage for images (ads, avatars, store logos, etc.)
- **CloudFront**: CDN for fast, global content delivery with caching
- **Local Storage**: Fallback for development (no AWS credentials needed)

## Benefits of S3 + CloudFront

### AWS S3
- **Scalable**: Handles unlimited storage
- **Reliable**: 99.999999999% (11 9's) durability
- **Cost-effective**: Pay only for what you use
- **Secure**: Fine-grained access control with IAM
- **Organized**: Folder-based structure (ads/, avatars/, stores/)

### CloudFront CDN
- **Fast**: Edge locations worldwide reduce latency
- **Cached**: Static content served from edge cache
- **Secure**: HTTPS by default
- **Optimized**: Automatic compression and optimization
- **Cost-effective**: Reduces S3 data transfer costs

## Configuration

### Development (Local Storage)
No AWS configuration needed. Files are stored in `uploads/` directory.

```yaml
# application-dev.yaml
media:
  storage:
    provider: local
    local:
      upload-dir: uploads
      base-url: http://localhost:8080/uploads
```

### Production (S3 + CloudFront)

```yaml
# application-prod.yaml
media:
  storage:
    provider: s3

aws:
  access-key-id: ${AWS_ACCESS_KEY_ID}
  secret-access-key: ${AWS_SECRET_ACCESS_KEY}
  s3:
    bucket-name: ${AWS_S3_BUCKET_NAME}
    region: ${AWS_S3_REGION:us-east-1}
  cloudfront:
    domain: ${AWS_CLOUDFRONT_DOMAIN}
```

### Environment Variables
```.env
AWS_ACCESS_KEY_ID=AKIAxxxxxxxxxxxxx
AWS_SECRET_ACCESS_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
AWS_S3_BUCKET_NAME=gamers-station-marketplace
AWS_S3_REGION=us-east-1
AWS_CLOUDFRONT_DOMAIN=d111111abcdef8.cloudfront.net
```

## AWS Setup Instructions

### Step 1: Create S3 Bucket

1. Go to AWS Console → S3
2. Click "Create bucket"
3. Choose a unique name: `gamers-station-marketplace`
4. Region: Choose closest to your users (e.g., `us-east-1`)
5. **Block Public Access**: Keep enabled (CloudFront will handle access)
6. **Versioning**: Disabled (optional: enable for backup)
7. **Encryption**: AES-256 (default)
8. Create bucket

### Step 2: Create Folder Structure

Create these folders in your S3 bucket:
- `ads/` - Ad images
- `avatars/` - User profile pictures
- `stores/` - Store logos and banners
- `categories/` - Category icons (future)

### Step 3: Configure Bucket Policy

Go to bucket → Permissions → Bucket Policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowCloudFrontAccess",
      "Effect": "Allow",
      "Principal": {
        "Service": "cloudfront.amazonaws.com"
      },
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::gamers-station-marketplace/*",
      "Condition": {
        "StringEquals": {
          "AWS:SourceArn": "arn:aws:cloudfront::ACCOUNT_ID:distribution/DISTRIBUTION_ID"
        }
      }
    }
  ]
}
```

*(Replace `ACCOUNT_ID` and `DISTRIBUTION_ID` after creating CloudFront)*

### Step 4: Create IAM User for API

1. Go to IAM → Users → Add user
2. User name: `gamers-station-api`
3. Access type: Programmatic access
4. Permissions: Attach policy directly
5. Create custom policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::gamers-station-marketplace",
        "arn:aws:s3:::gamers-station-marketplace/*"
      ]
    }
  ]
}
```

6. Save **Access Key ID** and **Secret Access Key** (you'll need these for .env)

### Step 5: Create CloudFront Distribution

1. Go to CloudFront → Create Distribution
2. **Origin domain**: Select your S3 bucket from dropdown
3. **Origin access**: Origin access control (OAC)
4. **Create new OAC**: Yes
5. **Name**: `gamers-station-s3-oac`
6. **Sign requests**: Yes
7. **Viewer protocol policy**: Redirect HTTP to HTTPS
8. **Allowed HTTP methods**: GET, HEAD, OPTIONS
9. **Cache key and origin requests**: CachingOptimized (recommended)
10. **Compress objects automatically**: Yes
11. **Price class**: Use all edge locations (best performance)
12. **Alternate domain names (CNAMEs)**: `media.thegamersstation.com` (optional)
13. **SSL certificate**: 
    - Use CloudFront certificate (default), OR
    - Request/import custom certificate for custom domain
14. **Default root object**: Leave empty
15. Create distribution

16. **Important**: Copy the distribution domain name (e.g., `d111111abcdef8.cloudfront.net`)
17. Go back to S3 bucket policy and add the CloudFront ARN (step 3 above)

### Step 6: Configure Custom Domain (Optional)

If you want `media.thegamersstation.com` instead of CloudFront domain:

1. Request SSL certificate in AWS Certificate Manager (ACM)
   - Region: **us-east-1** (required for CloudFront)
   - Domain: `media.thegamersstation.com`
   - Validation: DNS validation
   - Add CNAME records in your DNS provider

2. Update CloudFront distribution:
   - Add `media.thegamersstation.com` to Alternate Domain Names
   - Select your ACM certificate

3. Update DNS:
   - Add CNAME record: `media.thegamersstation.com` → `d111111abcdef8.cloudfront.net`

4. Update .env:
   ```
   AWS_CLOUDFRONT_DOMAIN=media.thegamersstation.com
   ```

## How It Works

### Upload Flow

1. User uploads image via API endpoint
2. API validates file (type, size, extension)
3. API generates unique filename: `UUID.ext`
4. API uploads to S3: `s3://bucket/folder/UUID.ext`
5. API returns CloudFront URL: `https://d111111abcdef8.cloudfront.net/folder/UUID.ext`
6. URL is stored in database (ads, users, etc.)

### Retrieval Flow

1. Client requests image URL
2. Request goes to CloudFront edge location
3. If cached: Served immediately from edge
4. If not cached: CloudFront fetches from S3, caches, and serves
5. Subsequent requests served from edge cache (fast!)

### Delete Flow

1. API calls delete method
2. API extracts S3 key from URL
3. API deletes object from S3
4. CloudFront cache invalidates automatically (after TTL)

## URL Patterns

### CloudFront URL
```
https://d111111abcdef8.cloudfront.net/ads/550e8400-e29b-41d4-a716-446655440000.jpg
```

### S3 Direct URL (fallback if CloudFront not configured)
```
https://gamers-station-marketplace.s3.us-east-1.amazonaws.com/ads/550e8400-e29b-41d4-a716-446655440000.jpg
```

### Local Development URL
```
http://localhost:8080/uploads/ads/550e8400-e29b-41d4-a716-446655440000.jpg
```

## API Usage

### Upload Image
```java
@Autowired
private MediaService mediaService;

public String uploadAdImage(MultipartFile file) {
    return mediaService.uploadImage(file, "ads");
}
```

### Upload Multiple Images
```java
List<String> imageUrls = mediaService.uploadImages(files, "ads");
```

### Delete Image
```java
mediaService.deleteImage(imageUrl);
```

## Cost Estimation (Monthly)

### S3 Storage
- First 50 TB: $0.023 per GB
- Example: 100 GB = $2.30/month

### S3 Requests
- PUT requests: $0.005 per 1,000 requests
- GET requests: $0.0004 per 1,000 requests
- Example: 100K uploads + 1M views = $0.50 + $0.40 = $0.90/month

### CloudFront Data Transfer
- First 10 TB: $0.085 per GB
- Example: 500 GB = $42.50/month

### Total Estimated Cost
- **Small scale** (10 GB, 10K uploads, 100K views): ~$5/month
- **Medium scale** (100 GB, 100K uploads, 1M views): ~$45/month
- **Large scale** (1 TB, 1M uploads, 10M views): ~$450/month

## Best Practices

1. **Always use CloudFront**: Never expose S3 URLs directly to users
2. **Set cache headers**: Configure appropriate TTL for different content types
3. **Compress images**: Use thumbnails for listings, full-size for detail views
4. **Implement lifecycle policies**: Archive old/unused images to S3 Glacier
5. **Monitor costs**: Set up CloudWatch alerts for unexpected usage spikes
6. **Enable access logging**: Track usage patterns and potential abuse
7. **Use presigned URLs**: For direct client uploads (future enhancement)

## Troubleshooting

### Images not uploading
- Check AWS credentials in `.env`
- Verify IAM user has `s3:PutObject` permission
- Check bucket name and region are correct

### Images not loading
- Verify CloudFront distribution is deployed (can take 15-30 minutes)
- Check S3 bucket policy allows CloudFront access
- Test S3 direct URL (replace CloudFront domain with S3 URL)

### Slow image loading
- CloudFront may not be caching (check cache headers)
- Edge location may be far from users (consider different price class)
- Images may be too large (implement compression/thumbnails)

## Security Considerations

1. **Never make S3 bucket public** - Always use CloudFront
2. **Use IAM roles in production** - Instead of access keys
3. **Rotate access keys regularly** - At least every 90 days
4. **Enable S3 versioning** - For critical buckets (optional)
5. **Set up CloudWatch alarms** - For unusual activity
6. **Validate uploads server-side** - Never trust client input
7. **Implement rate limiting** - Prevent upload abuse

## Future Enhancements

- [ ] Image compression and thumbnail generation
- [ ] Presigned URLs for direct browser uploads
- [ ] CloudFront signed URLs for private content
- [ ] S3 lifecycle policies for archiving old images
- [ ] Lambda@Edge for image resizing on-the-fly
- [ ] WAF rules to prevent abuse
- [ ] Multi-region failover
