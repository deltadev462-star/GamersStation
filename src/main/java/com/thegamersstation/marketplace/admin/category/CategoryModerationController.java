package com.thegamersstation.marketplace.admin.category;

import com.thegamersstation.marketplace.category.dto.CategoryTreeDto;
import com.thegamersstation.marketplace.category.dto.CreateCategoryDto;
import com.thegamersstation.marketplace.category.dto.ReorderCategoriesDto;
import com.thegamersstation.marketplace.category.dto.UpdateCategoryDto;
import com.thegamersstation.marketplace.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin - Categories", description = "Admin endpoints for category management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryModerationController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    @Operation(
        summary = "Get category details by ID (Admin)",
        description = "Retrieves detailed information about a specific category including its hierarchy."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryTreeDto> getCategoryById(@PathVariable Long id) {
        CategoryTreeDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @Operation(
        summary = "Create category (Admin)",
        description = "Creates a new category. Level is determined by parent. Maximum 3 levels."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or maximum depth exceeded"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "404", description = "Parent category not found")
    })
    public ResponseEntity<CategoryTreeDto> createCategory(@Valid @RequestBody CreateCategoryDto createDto) {
        CategoryTreeDto category = categoryService.createCategory(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update category (Admin)",
        description = "Updates category details. Validates level constraints when moving categories."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or constraint violation"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryTreeDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryDto updateDto) {
        CategoryTreeDto category = categoryService.updateCategory(id, updateDto);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete category (Admin)",
        description = "Deletes a category. Cannot delete if it has children or associated posts."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete category with children or posts"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    @Operation(
        summary = "Reorder categories (Admin)",
        description = "Updates sort order of categories within the same parent level."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Categories reordered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or categories don't share same parent"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> reorderCategories(@Valid @RequestBody ReorderCategoriesDto reorderDto) {
        categoryService.reorderCategories(reorderDto);
        return ResponseEntity.noContent().build();
    }
}
