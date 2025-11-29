package com.thegamersstation.marketplace.admin.store;

import com.thegamersstation.marketplace.store.dto.CreateStoreRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a store manager with their store")
public class CreateStoreManagerRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Schema(description = "Store manager phone number", example = "+966501234567", required = true)
    private String phoneNumber;
    
    @Size(max = 50, message = "Username must not exceed 50 characters")
    @Schema(description = "Store manager username", example = "gaming_pro")
    private String username;
    
    @Schema(description = "Store manager email", example = "contact@gamingpro.com")
    private String email;
    
    @Schema(description = "Store manager city ID", example = "1")
    private Long cityId;
    
    @NotNull(message = "Store information is required")
    @Valid
    @Schema(description = "Store details", required = true)
    private CreateStoreRequest store;
}
