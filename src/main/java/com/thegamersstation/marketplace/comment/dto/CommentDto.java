package com.thegamersstation.marketplace.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    
    private Long id;
    private Long postId;
    private CommentAuthorDto author;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Nested DTO for comment author
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentAuthorDto {
        private Long id;
        private String username;
        private String avatarUrl;
    }
}
