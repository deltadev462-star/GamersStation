package com.thegamersstation.marketplace.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsPageResponseDto {
    
    private List<CommentDto> comments;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Long nextCursor;  // ID of last comment, null if no more
        private Boolean hasMore;
        private Long total;       // Total comment count for the post
    }
}
