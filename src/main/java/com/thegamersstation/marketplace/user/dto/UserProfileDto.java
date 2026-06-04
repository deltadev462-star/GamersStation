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

    @Schema(description = "City information")
    private CityInfo city;

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

    @Schema(description = "Profile image URL")
    private String profileImage;

    @Schema(description = "Background image URL")
    private String backgroundImage;

    @Schema(description = "Social media links")
    @Builder.Default
    private SocialLinksDto socialLinks = new SocialLinksDto();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "City information")
    public static class CityInfo {
        @Schema(description = "City ID", example = "1")
        private Long id;

        @Schema(description = "City name in English", example = "Riyadh")
        private String nameEn;

        @Schema(description = "City name in Arabic", example = "الرياض")
        private String nameAr;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Social media links")
    public static class SocialLinksDto {
        @Schema(description = "Facebook profile URL")
        private String facebook;

        @Schema(description = "Twitter profile URL")
        private String twitter;

        @Schema(description = "Instagram profile URL")
        private String instagram;

        @Schema(description = "YouTube channel URL")
        private String youtube;

        @Schema(description = "LinkedIn profile URL")
        private String linkedin;

        @Schema(description = "GitHub profile URL")
        private String github;

        @Schema(description = "Personal website URL")
        private String website;
    }
}
