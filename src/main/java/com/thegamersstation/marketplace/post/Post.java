package com.thegamersstation.marketplace.post;

import com.thegamersstation.marketplace.category.Category;
import com.thegamersstation.marketplace.city.City;
import com.thegamersstation.marketplace.store.Store;
import com.thegamersstation.marketplace.user.repository.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Post entity representing marketplace posts.
 * 
 * <p>Each post is owned by a User (either normal user or store owner/manager).
 * The post is directly linked to the user account, not to a store entity.
 * This allows any user type to create posts regardless of whether they manage a store.</p>
 * 
 * <p>Price fields (price, priceMin, priceMax) are optional:
 * - For SELL posts: typically has a fixed price
 * - For ASK/REQUEST posts: may have no price, or a price range (min/max)
 * - Some posts may not specify price at all (e.g., "make an offer")</p>
 */
@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this post.
     * Can be a normal USER, STORE_MANAGER, or even ADMIN.
     * The user's role doesn't restrict post creation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * The store this post belongs to (optional).
     * Only set if the owner is a STORE_MANAGER.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Fixed price for the post (optional).
     * Typically used for SELL type posts.
     * Can be null for "make an offer" scenarios.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Minimum acceptable price (optional).
     * Used when seller accepts price range or for ASK posts.
     */
    @Column(name = "price_min", precision = 10, scale = 2)
    private BigDecimal priceMin;

    /**
     * Maximum price range (optional).
     * Used when buyer specifies budget range for ASK posts.
     */
    @Column(name = "price_max", precision = 10, scale = 2)
    private BigDecimal priceMax;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "SAR";

    @Enumerated(EnumType.STRING)
    @Column(name = "`condition`")
    private PostCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.WAITING_APPROVAL;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PostType {
        SELL, ASK
    }

    public enum PostCondition {
        NEW, LIKE_NEW, USED_GOOD, USED_FAIR, FOR_PARTS
    }

    public enum PostStatus {
        WAITING_APPROVAL, ACTIVE, SOLD, BLOCKED, DELETED
    }
}
