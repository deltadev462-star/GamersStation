package com.thegamersstation.marketplace.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update category request")
public class UpdateCategoryDto {

    @Schema(description = "Category name in English", example = "PlayStation")
    @Size(max = 100, message = "English name must not exceed 100 characters")
    private String nameEn;

    @Schema(description = "Category name in Arabic", example = "بلايستيشن")
    @Size(max = 100, message = "Arabic name must not exceed 100 characters")
    private String nameAr;

    @Schema(description = "URL-friendly slug", example = "playstation")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    @Schema(description = "Parent category ID", example = "1")
    private Long parentId;

    @Schema(description = "Sort order within parent", example = "0")
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;

    @Schema(description = "Is category active", example = "true")
    private Boolean isActive;
}
