package com.thegamersstation.marketplace.comment;

import com.thegamersstation.marketplace.comment.dto.CommentDto;
import com.thegamersstation.marketplace.comment.dto.CommentsPageResponseDto;
import com.thegamersstation.marketplace.comment.dto.CreateCommentRequest;
import com.thegamersstation.marketplace.comment.dto.UpdateCommentRequest;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.common.util.ContentSanitizer;
import com.thegamersstation.marketplace.post.Post;
import com.thegamersstation.marketplace.post.PostRepository;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final CommentMapper commentMapper;
    private final ContentSanitizer contentSanitizer;
    
    /**
     * Create a new comment on a post
     */
    @Transactional
    public CommentDto createComment(Long postId, CreateCommentRequest request, Long userId) {
        User author = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        // Sanitize content
        String sanitizedContent = contentSanitizer.sanitize(request.getContent());
        
        // Create comment
        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(sanitizedContent)
                .isDeleted(false)
                .build();
        
        Comment savedComment = commentRepository.save(comment);

        postRepository.save(post);
        
        log.info("User {} created comment {} on post {}", userId, savedComment.getId(), postId);
        
        return commentMapper.toDto(savedComment);
    }
    
    /**
     * Get comments for a post with cursor-based pagination
     */
    @Transactional(readOnly = true)
    public CommentsPageResponseDto getComments(Long postId, Long cursor, int limit) {
        // Validate post exists
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        // Fetch comments with cursor
        List<Comment> comments = commentRepository.findByPostIdWithCursor(
                postId, 
                cursor, 
                PageRequest.of(0, limit)
        );
        
        // Convert to DTOs
        List<CommentDto> commentDtos = comments.stream()
                .map(commentMapper::toDto)
                .toList();
        
        // Determine next cursor and hasMore
        Long nextCursor = null;
        boolean hasMore = false;
        
        if (!comments.isEmpty()) {
            nextCursor = comments.get(comments.size() - 1).getId();
            // Check if there are more comments
            List<Comment> nextPage = commentRepository.findByPostIdWithCursor(
                    postId, 
                    nextCursor, 
                    PageRequest.of(0, 1)
            );
            hasMore = !nextPage.isEmpty();
        }
        
        // Build response
        return CommentsPageResponseDto.builder()
                .comments(commentDtos)
                .pagination(CommentsPageResponseDto.PaginationInfo.builder()
                        .nextCursor(hasMore ? nextCursor : null)
                        .hasMore(hasMore)
                        .build())
                .build();
    }
    
    /**
     * Update a comment
     */
    @Transactional
    public CommentDto updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        // Check ownership
        if (!comment.isAuthor(userId)) {
            throw new AccessDeniedException("You can only update your own comments");
        }
        
        // Sanitize and update content
        String sanitizedContent = contentSanitizer.sanitize(request.getContent());
        comment.setContent(sanitizedContent);
        
        Comment updatedComment = commentRepository.save(comment);
        
        log.info("User {} updated comment {}", userId, commentId);
        
        return commentMapper.toDto(updatedComment);
    }
    
    /**
     * Delete a comment (soft delete)
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        // Check ownership
        if (!comment.isAuthor(userId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        
        // Soft delete
        comment.softDelete();
        commentRepository.save(comment);
        
        // Decrement post comment count
        Post post = comment.getPost();
        postRepository.save(post);
        
        log.info("User {} deleted comment {}", userId, commentId);
    }
    
    /**
     * Get a single comment by ID
     */
    @Transactional(readOnly = true)
    public CommentDto getComment(Long commentId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        return commentMapper.toDto(comment);
    }
}
