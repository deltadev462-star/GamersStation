package com.thegamersstation.marketplace.post.dto;

import com.thegamersstation.marketplace.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private Long ownerId;
    private String ownerUsername;
    
    @Schema(description = "Store information (only present for store posts)")
    private StoreInfoDto store;
    
    private Post.PostType type;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String currency;
    private Post.PostCondition condition;
    private Long categoryId;
    private String categoryName;
    private Long cityId;
    private String cityName;
    private Post.PostStatus status;
    private List<PostImageDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Basic store information for posts")
    public static class StoreInfoDto {
        private Long id;
        private String nameEn;
        private String nameAr;
        private String slug;
        private String logoUrl;
        private Boolean isVerified;
    }
}
