package com.thegamersstation.marketplace.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.status <> 'DELETED'")
    Optional<Post> findByIdAndNotDeleted(@Param("id") Long id);
    
    @Query("SELECT p FROM Post p WHERE p.owner.id = :ownerId AND p.status <> 'DELETED'")
    Page<Post> findByOwnerIdAndNotDeleted(@Param("ownerId") Long ownerId, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.status = :status")
    Page<Post> findByStatus(@Param("status") Post.PostStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:cityId IS NULL OR p.city.id = :cityId) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:condition IS NULL OR p.condition = :condition)")
    Page<Post> searchPosts(
        @Param("categoryId") Long categoryId,
        @Param("cityId") Long cityId,
        @Param("type") Post.PostType type,
        @Param("condition") Post.PostCondition condition,
        Pageable pageable
    );
    
    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' " +
           "AND (:query IS NULL OR :query = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:cityId IS NULL OR p.city.id = :cityId) " +
           "AND (:regionId IS NULL OR p.city.region.id = :regionId) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:condition IS NULL OR p.condition = :condition) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Post> advancedSearchPosts(
        @Param("query") String query,
        @Param("categoryId") Long categoryId,
        @Param("cityId") Long cityId,
        @Param("regionId") Long regionId,
        @Param("type") Post.PostType type,
        @Param("condition") Post.PostCondition condition,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.owner.id = :ownerId AND p.status = 'ACTIVE'")
    long countActivePostsByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT p FROM Post p WHERE p.store.id = :storeId AND p.status = 'ACTIVE'")
    Page<Post> findByStoreIdAndActive(@Param("storeId") Long storeId, Pageable pageable);
}
