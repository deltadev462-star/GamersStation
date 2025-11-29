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
@Schema(description = "Create category request")
public class CreateCategoryDto {

    @Schema(description = "Category name in English", example = "PlayStation", required = true)
    @NotBlank(message = "English name is required")
    @Size(max = 100, message = "English name must not exceed 100 characters")
    private String nameEn;

    @Schema(description = "Category name in Arabic", example = "بلايستيشن", required = true)
    @NotBlank(message = "Arabic name is required")
    @Size(max = 100, message = "Arabic name must not exceed 100 characters")
    private String nameAr;

    @Schema(description = "URL-friendly slug (auto-generated if not provided)", example = "playstation")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    @Schema(description = "Parent category ID (null for level 1)", example = "1")
    private Long parentId;

    @Schema(description = "Sort order within parent", example = "0")
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;
}
