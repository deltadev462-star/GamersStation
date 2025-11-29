package com.thegamersstation.marketplace.comment;

import com.thegamersstation.marketplace.post.Post;
import com.thegamersstation.marketplace.user.repository.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Comment entity representing user comments on posts.
 * 
 * <p>Comments support:
 * - Flat structure (no nesting in Phase 1)
 * - Soft delete (is_deleted flag)
 * - Cursor-based pagination
 * - Content validation (max 1000 chars)
 * </p>
 */
@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The post this comment belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    /**
     * The user who authored this comment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    /**
     * Comment content (max 1000 characters)
     */
    @Column(nullable = false, length = 1000)
    private String content;
    
    /**
     * Soft delete flag
     * When true, comment is hidden but preserved for data integrity
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    
    /**
     * Timestamp when comment was soft deleted
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Soft delete this comment
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * Check if user is the author of this comment
     */
    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }
}
