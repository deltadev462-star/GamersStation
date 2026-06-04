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

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Schema(description = "Email address", example = "user@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "City ID (required for profile completion)", example = "1")
    @Positive(message = "City ID must be positive")
    private Long cityId;

    @Schema(description = "Profile image URL", example = "https://example.com/profile.jpg")
    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Profile image must be a valid HTTP(S) URL")
    private String profileImage;

    @Schema(description = "Background image URL", example = "https://example.com/background.jpg")
    @Size(max = 500, message = "Background image URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Background image must be a valid HTTP(S) URL")
    private String backgroundImage;

    @Schema(description = "Facebook profile URL", example = "https://facebook.com/username")
    @Size(max = 200, message = "Facebook link must not exceed 200 characters")
    @Pattern(regexp = "^https?://.*", message = "Facebook link must be a valid HTTP(S) URL")
    private String facebookLink;

    @Schema(description = "Twitter profile URL", example = "https://twitter.com/username")
    @Size(max = 200, message = "Twitter link must not exceed 200 characters")
    @Pattern(regexp = "^https?://.*", message = "Twitter link must be a valid HTTP(S) URL")
    private String twitterLink;

    @Schema(description = "Instagram profile URL", example = "https://instagram.com/username")
    @Size(max = 200, message = "Instagram link must not exceed 200 characters")
    @Pattern(regexp = "^https?://.*", message = "Instagram link must be a valid HTTP(S) URL")
    private String instagramLink;

    @Schema(description = "YouTube channel URL", example = "https://youtube.com/channel/channelId")
    @Size(max = 200, message = "YouTube link must not exceed 200 characters")
    @Pattern(regexp = "^https?://.*", message = "YouTube link must be a valid HTTP(S) URL")
    private String youtubeLink;

    @Schema(description = "LinkedIn profile URL", example = "https://linkedin.com/in/username")
    @Size(max = 200, message = "LinkedIn link must not exceed 200 characters")
    @Pattern(regexp = "^https?://.*", message = "LinkedIn link must be a valid HTTP(S) URL")
    private String linkedinLink;

    @Schema(description = "GitHub profile URL", example = "https://github.com/username")
    @Size(max = 200, message = "GitHub link must not exceed 200 characters")
    @Pattern(regexp = "^https?://.*", message = "GitHub link must be a valid HTTP(S) URL")
    private String githubLink;

    @Schema(description = "Personal website URL", example = "https://example.com")
    @Size(max = 200, message = "Website link must not exceed 200 characters")
    @Pattern(regexp = "^https?://.*", message = "Website link must be a valid HTTP(S) URL")
    private String websiteLink;
}
