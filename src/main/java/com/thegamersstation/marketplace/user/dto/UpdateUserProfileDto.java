package com.thegamersstation.marketplace.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update user profile request")
public class UpdateUserProfileDto {

    @Schema(description = "Username (3-50 characters, alphanumeric with underscores)", example = "gamer123")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Schema(description = "Email address", example = "user@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "City ID (required for profile completion)", example = "1", required = true)
    @NotNull(message = "City ID is required")
    @Positive(message = "City ID must be positive")
    private Long cityId;
}
