package com.thegamersstation.marketplace.post;

import com.thegamersstation.marketplace.post.dto.PostDto;
import com.thegamersstation.marketplace.post.dto.CreatePostRequest;
import com.thegamersstation.marketplace.post.dto.MarkAsSoldRequest;
import com.thegamersstation.marketplace.post.dto.UpdatePostRequest;
import com.thegamersstation.marketplace.common.dto.PageResponseDto;
import com.thegamersstation.marketplace.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management endpoints")
public class PostController {
    
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "createdAt", "price", "title", "updatedAt"
    );
    
    private final PostService PostService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new Post")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody CreatePostRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostDto Post = PostService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Post);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an Post")
    public ResponseEntity<PostDto> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePostRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostDto Post = PostService.updatePost(id, request, userId);
        return ResponseEntity.ok(Post);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Post by ID")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        PostDto Post = PostService.getPostById(id);
        return ResponseEntity.ok(Post);
    }
    
    @GetMapping
    @Operation(summary = "Search posts with filters")
    public ResponseEntity<PageResponseDto<PostDto>> searchPosts(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String categoryIds,
        @RequestParam(required = false) Long cityId,
        @RequestParam(required = false) Post.PostType type,
        @RequestParam(required = false) Post.PostCondition condition,
        @RequestParam(required = false) java.math.BigDecimal minPrice,
        @RequestParam(required = false) java.math.BigDecimal maxPrice,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(direction, safeSortBy));
        PageResponseDto<PostDto> ads = PostService.searchPosts(categoryId, categoryIds, cityId, type, condition, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ads);
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Advanced post search",
        description = "Search posts with full-text search, filters, price range, and sorting. " +
                     "Supports sorting by: createdAt, price, title. Add 'Desc' suffix for descending (e.g., 'priceDesc', 'newest')."
    )
    public ResponseEntity<PageResponseDto<PostDto>> advancedSearch(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long cityId,
        @RequestParam(required = false) Long regionId,
        @RequestParam(required = false) Post.PostType type,
        @RequestParam(required = false) Post.PostCondition condition,
        @RequestParam(required = false) java.math.BigDecimal minPrice,
        @RequestParam(required = false) java.math.BigDecimal maxPrice,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "newest") String sort
    ) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        // Parse sort parameter
        Sort sorting = parseSortParameter(sort);
        Pageable pageable = PageRequest.of(page, safeSize, sorting);
        
        PageResponseDto<PostDto> posts = PostService.advancedSearchPosts(
            q, categoryId, cityId, regionId, type, condition, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(posts);
    }
    
    private Sort parseSortParameter(String sort) {
        return switch (sort.toLowerCase()) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "price_asc", "priceasc", "cheapest" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc", "pricedesc", "expensive" -> Sort.by(Sort.Direction.DESC, "price");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
    
    @GetMapping("/my-ads")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my posts")
    public ResponseEntity<PageResponseDto<PostDto>> getMyPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        Long userId = SecurityUtil.getCurrentUserId();
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(direction, safeSortBy));
        PageResponseDto<PostDto> ads = PostService.getMyPosts(userId, pageable);
        return ResponseEntity.ok(ads);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete an Post")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/mark-sold")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark Post as sold")
    public ResponseEntity<Void> markAsSold(
        @PathVariable Long id,
        @Valid @RequestBody MarkAsSoldRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostService.markAsSold(id, userId, request.getSoldThroughPlatform());
        return ResponseEntity.noContent().build();
    }
}
