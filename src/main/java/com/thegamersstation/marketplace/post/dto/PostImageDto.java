package com.thegamersstation.marketplace.post.dto;

import lombok.Data;

@Data
public class PostImageDto {
    private Long id;
    private String url;
    private String thumbnailUrl;
    private Integer sortOrder;
}
