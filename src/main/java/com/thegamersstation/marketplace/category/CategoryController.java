package com.thegamersstation.marketplace.category;

import com.thegamersstation.marketplace.category.dto.CategoryTreeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category browsing endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/tree")
    @Operation(
        summary = "Get category tree",
        description = "Returns hierarchical category tree with up to 3 levels. Includes bilingual names (EN/AR)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category tree retrieved successfully")
    })
    public ResponseEntity<List<CategoryTreeDto>> getCategoryTree() {
        List<CategoryTreeDto> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/debug/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Debug: List all categories with full details",
        description = "Returns a flat list of ALL categories with their IDs, names, slugs, and active status. Use this to debug category issues."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All categories retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getAllCategoriesDebug() {
        List<Category> allCategories = categoryRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalCategories", allCategories.size());
        response.put("categories", allCategories.stream()
            .map(cat -> {
                Map<String, Object> catMap = new HashMap<>();
                catMap.put("id", cat.getId());
                catMap.put("nameEn", cat.getNameEn());
                catMap.put("nameAr", cat.getNameAr());
                catMap.put("slug", cat.getSlug());
                catMap.put("parentId", cat.getParentId());
                catMap.put("level", cat.getLevel());
                catMap.put("sortOrder", cat.getSortOrder());
                catMap.put("isActive", cat.getIsActive());
                return catMap;
            })
            .collect(Collectors.toList()));
        
        // Group by parent for easier debugging
        Map<String, List<Map<String, Object>>> grouped = allCategories.stream()
            .collect(Collectors.groupingBy(
                cat -> cat.getParentId() == null ? "root" : "child-of-" + cat.getParentId(),
                Collectors.mapping(cat -> {
                    Map<String, Object> catMap = new HashMap<>();
                    catMap.put("id", cat.getId());
                    catMap.put("nameEn", cat.getNameEn());
                    catMap.put("slug", cat.getSlug());
                    catMap.put("isActive", cat.getIsActive());
                    return catMap;
                }, Collectors.toList())
            ));
        response.put("groupedByParent", grouped);
        
        return ResponseEntity.ok(response);
    }
}
