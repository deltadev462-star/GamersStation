package com.thegamersstation.marketplace.user.dto;

import com.thegamersstation.marketplace.user.repository.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User profile information")
public class UserProfileDto {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Phone number in E.164 format", example = "+966501234567")
    private String phoneNumber;

    @Schema(description = "Unique username", example = "gamer123")
    private String username;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "City ID", example = "1")
    private Long cityId;

    @Schema(description = "User role", example = "USER")
    private User.UserRole role;

    @Schema(description = "Is user account active", example = "true")
    private Boolean isActive;

    @Schema(description = "Is user profile completed", example = "true")
    private Boolean profileCompleted;

    @Schema(description = "Account creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}
