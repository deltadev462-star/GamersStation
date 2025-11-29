package com.thegamersstation.marketplace.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Category tree node with children")
public class CategoryTreeDto {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name in English", example = "Consoles")
    private String nameEn;

    @Schema(description = "Category name in Arabic", example = "أجهزة الألعاب")
    private String nameAr;

    @Schema(description = "URL-friendly slug", example = "consoles")
    private String slug;

    @Schema(description = "Sort order", example = "0")
    private Integer sortOrder;

    @Schema(description = "Is category active", example = "true")
    private Boolean isActive;

    @Schema(description = "Child categories")
    @Builder.Default
    private List<CategoryTreeDto> children = new ArrayList<>();
}
