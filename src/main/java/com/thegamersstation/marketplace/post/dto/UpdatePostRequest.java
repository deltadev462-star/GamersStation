package com.thegamersstation.marketplace.post.dto;

import com.thegamersstation.marketplace.post.Post;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePostRequest {
    
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum price must be greater than 0")
    private BigDecimal priceMin;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum price must be greater than 0")
    private BigDecimal priceMax;
    
    private Post.PostCondition condition;
    
    private Long cityId;
    
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;
}
