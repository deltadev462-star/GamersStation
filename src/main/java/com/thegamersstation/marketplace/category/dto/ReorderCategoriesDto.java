package com.thegamersstation.marketplace.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Reorder categories within a parent")
public class ReorderCategoriesDto {

    @Schema(description = "Parent category ID (null for level 1)", example = "1")
    private Long parentId;

    @Schema(description = "Ordered list of category IDs", example = "[3, 1, 2]", required = true)
    @NotEmpty(message = "Category IDs list cannot be empty")
    private List<Long> categoryIds;
}
