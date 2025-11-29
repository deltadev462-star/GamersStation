package com.thegamersstation.marketplace.category;

import com.thegamersstation.marketplace.category.dto.CategoryTreeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category browsing endpoints")
public class CategoryController {

    private final CategoryService categoryService;

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
}
