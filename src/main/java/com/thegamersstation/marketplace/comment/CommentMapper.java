package com.thegamersstation.marketplace.comment;

import com.thegamersstation.marketplace.comment.dto.CommentDto;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    
    /**
     * Convert Comment entity to CommentDto
     */
    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        
        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .author(CommentDto.CommentAuthorDto.builder()
                        .id(comment.getAuthor().getId())
                        .username(comment.getAuthor().getUsername())
                        .avatarUrl(null)  // TODO: Add avatarUrl field to User entity
                        .build())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
