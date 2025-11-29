package com.thegamersstation.marketplace.post.dto;

import com.thegamersstation.marketplace.post.Post;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatePostRequest {
    
    @NotNull(message = "Post type is required")
    private Post.PostType type;
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum price must be greater than 0")
    private BigDecimal priceMin;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum price must be greater than 0")
    private BigDecimal priceMax;
    
    private Post.PostCondition condition;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    @NotNull(message = "City is required")
    private Long cityId;
    
    @NotEmpty(message = "At least one image is required")
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;
}
