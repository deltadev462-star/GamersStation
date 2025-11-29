package com.thegamersstation.marketplace.city.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "City information")
public class CityDto {
    
    @Schema(description = "City ID", example = "1")
    private Long id;
    
    @Schema(description = "City name in English", example = "Riyadh")
    private String nameEn;
    
    @Schema(description = "City name in Arabic", example = "الرياض")
    private String nameAr;
    
    @Schema(description = "URL-friendly city slug", example = "riyadh")
    private String slug;
}
