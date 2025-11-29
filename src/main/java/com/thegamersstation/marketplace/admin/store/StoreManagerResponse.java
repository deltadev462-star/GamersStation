package com.thegamersstation.marketplace.admin.store;

import com.thegamersstation.marketplace.store.dto.StoreDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing created store manager and store information")
public class StoreManagerResponse {
    
    @Schema(description = "Created user ID")
    private Long userId;
    
    @Schema(description = "User phone number")
    private String phoneNumber;
    
    @Schema(description = "User username")
    private String username;
    
    @Schema(description = "Created store")
    private StoreDto store;
}
