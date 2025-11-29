package com.thegamersstation.marketplace.admin.post;

import com.thegamersstation.marketplace.post.Post;
import com.thegamersstation.marketplace.post.dto.PostDto;
import com.thegamersstation.marketplace.common.dto.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Posts", description = "Admin Post moderation endpoints")
public class PostModerationController {
    
    private final PostModerationService postModerationService;
    
    @GetMapping("/pending")
    @Operation(summary = "Get pending posts for approval")
    public ResponseEntity<PageResponseDto<PostDto>> getPendingPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponseDto<PostDto> posts = postModerationService.getPendingPosts(pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping
    @Operation(summary = "Get all posts with optional status filter")
    public ResponseEntity<PageResponseDto<PostDto>> getAllPosts(
        @RequestParam(required = false) Post.PostStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponseDto<PostDto> posts = postModerationService.getAllPosts(status, pageable);
        return ResponseEntity.ok(posts);
    }
    
    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a pending post")
    public ResponseEntity<PostDto> approvePost(@PathVariable Long id) {
        PostDto post = postModerationService.approvePost(id);
        return ResponseEntity.ok(post);
    }
    
    @PostMapping("/{id}/block")
    @Operation(summary = "Block a post")
    public ResponseEntity<PostDto> blockPost(@PathVariable Long id) {
        PostDto post = postModerationService.blockPost(id);
        return ResponseEntity.ok(post);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a post")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postModerationService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
