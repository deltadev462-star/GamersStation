package com.thegamersstation.marketplace.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response with tokens")
public class AuthResponseDto {

    @Schema(description = "Access token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration in seconds", example = "900")
    private Long expiresIn;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Phone number", example = "+966501234567")
    private String phoneNumber;

    @Schema(description = "User role", example = "USER")
    private String role;

    @Schema(description = "Whether profile is completed", example = "false")
    private Boolean profileCompleted;

    @Schema(description = "Whether this is a new user (first login)", example = "true")
    private Boolean isNewUser;
}
