package com.thegamersstation.marketplace.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Pagination request parameters")
public class PageRequestDto {
    
    @Min(value = 0, message = "Page number cannot be negative")
    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    private int page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Number of items per page", example = "20", defaultValue = "20")
    private int size = 20;
    
    @Schema(description = "Sort field", example = "createdAt")
    private String sort;
    
    @Schema(description = "Sort direction (asc or desc)", example = "desc", defaultValue = "desc")
    private String direction = "desc";
}
