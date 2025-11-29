package com.thegamersstation.marketplace.media.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageUploadResponseDto {
    
    /**
     * Public URL of uploaded image
     */
    private String url;
    
    /**
     * Original filename
     */
    private String filename;
    
    /**
     * File size in bytes
     */
    private Long size;
    
    /**
     * Content type (MIME type)
     */
    private String contentType;
}
