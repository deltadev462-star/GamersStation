package com.thegamersstation.marketplace.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Region information")
public class RegionDto {
    
    @Schema(description = "Region ID", example = "1")
    private Long id;
    
    @Schema(description = "Region name in English", example = "Riyadh")
    private String nameEn;
    
    @Schema(description = "Region name in Arabic", example = "الرياض")
    private String nameAr;
    
    @Schema(description = "URL-friendly region slug", example = "riyadh")
    private String slug;
}
