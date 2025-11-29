package com.thegamersstation.marketplace.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Find comment by ID that is not deleted
     */
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Comment> findByIdAndNotDeleted(@Param("id") Long id);
    
    /**
     * Cursor-based pagination: Get comments for a post
     * Ordered by ID DESC (newest first)
     */
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id = :postId 
          AND c.isDeleted = false 
          AND (:cursor IS NULL OR c.id < :cursor)
        ORDER BY c.id DESC
        """)
    List<Comment> findByPostIdWithCursor(
        @Param("postId") Long postId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );
    
    /**
     * Count non-deleted comments for a post
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
    Long countByPostId(@Param("postId") Long postId);
    
    /**
     * Get user's comments (for profile page)
     */
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.author.id = :authorId 
          AND c.isDeleted = false
        ORDER BY c.createdAt DESC
        """)
    List<Comment> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
    
    /**
     * Count user's comments
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId AND c.isDeleted = false")
    Long countByAuthorId(@Param("authorId") Long authorId);
}
