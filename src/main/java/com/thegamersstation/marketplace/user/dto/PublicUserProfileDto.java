package com.thegamersstation.marketplace.user.dto;

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
@Schema(description = "Public user profile information (limited data)")
public class PublicUserProfileDto {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "gamer123")
    private String username;

    @Schema(description = "City ID", example = "1")
    private Long cityId;

    @Schema(description = "Account creation timestamp")
    private Instant createdAt;
}
