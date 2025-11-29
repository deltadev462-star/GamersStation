package com.thegamersstation.marketplace.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new store")
public class CreateStoreRequest {
    
    @NotBlank(message = "English name is required")
    @Size(max = 100, message = "English name must not exceed 100 characters")
    @Schema(description = "Store name in English", example = "Gaming Pro Store", required = true)
    private String nameEn;
    
    @NotBlank(message = "Arabic name is required")
    @Size(max = 100, message = "Arabic name must not exceed 100 characters")
    @Schema(description = "Store name in Arabic", example = "متجر جيمنج برو", required = true)
    private String nameAr;
    
    @Schema(description = "URL-friendly slug (auto-generated if not provided)", example = "gaming-pro-store")
    private String slug;
    
    @Schema(description = "Store description in English")
    private String descriptionEn;
    
    @Schema(description = "Store description in Arabic")
    private String descriptionAr;
    
    @Schema(description = "Store logo URL")
    private String logoUrl;
    
    @Schema(description = "Store banner URL")
    private String bannerUrl;
}
