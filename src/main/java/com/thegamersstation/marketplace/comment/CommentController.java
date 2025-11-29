package com.thegamersstation.marketplace.comment;

import com.thegamersstation.marketplace.comment.dto.CommentDto;
import com.thegamersstation.marketplace.comment.dto.CommentsPageResponseDto;
import com.thegamersstation.marketplace.comment.dto.CreateCommentRequest;
import com.thegamersstation.marketplace.comment.dto.UpdateCommentRequest;
import com.thegamersstation.marketplace.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * Create a comment on a post
     * POST /api/v1/posts/{postId}/comments
     */
    @PostMapping("/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        CommentDto comment = commentService.createComment(postId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    
    /**
     * Get comments for a post with cursor-based pagination
     * GET /api/v1/posts/{postId}/comments?cursor=&limit=20
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<CommentsPageResponseDto> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        // Limit max page size
        if (limit > 100) {
            limit = 100;
        }
        
        CommentsPageResponseDto comments = commentService.getComments(postId, cursor, limit);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * Get a single comment by ID
     * GET /api/v1/comments/{commentId}
     */
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long commentId) {
        CommentDto comment = commentService.getComment(commentId);
        return ResponseEntity.ok(comment);
    }
    /**
     * Update a comment
     * PUT /api/v1/comments/{commentId}
     */
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        CommentDto comment = commentService.updateComment(commentId, request, userId);
        return ResponseEntity.ok(comment);
    }
    
    /**
     * Delete a comment
     * DELETE /api/v1/comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
