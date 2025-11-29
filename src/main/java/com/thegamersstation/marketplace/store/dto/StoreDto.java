package com.thegamersstation.marketplace.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Store information")
public class StoreDto {
    
    @Schema(description = "Store ID", example = "1")
    private Long id;
    
    @Schema(description = "Store owner ID", example = "5")
    private Long ownerId;
    
    @Schema(description = "Store name in English", example = "Gaming Pro Store")
    private String nameEn;
    
    @Schema(description = "Store name in Arabic", example = "متجر جيمنج برو")
    private String nameAr;
    
    @Schema(description = "URL-friendly store slug", example = "gaming-pro-store")
    private String slug;
    
    @Schema(description = "Store description in English")
    private String descriptionEn;
    
    @Schema(description = "Store description in Arabic")
    private String descriptionAr;
    
    @Schema(description = "Store logo URL")
    private String logoUrl;
    
    @Schema(description = "Store banner URL")
    private String bannerUrl;
    
    @Schema(description = "Whether store is verified", example = "true")
    private Boolean isVerified;
    
    @Schema(description = "Whether store is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Store creation timestamp")
    private Instant createdAt;
    
    @Schema(description = "Store last update timestamp")
    private Instant updatedAt;
}
