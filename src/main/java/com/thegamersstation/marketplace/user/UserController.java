package com.thegamersstation.marketplace.user;

import com.thegamersstation.marketplace.security.SecurityUtil;
import com.thegamersstation.marketplace.user.dto.PublicUserProfileDto;
import com.thegamersstation.marketplace.user.dto.UpdateUserProfileDto;
import com.thegamersstation.marketplace.user.dto.UserProfileDto;
import com.thegamersstation.marketplace.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the authenticated user's complete profile including private information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserProfileDto> getCurrentUserProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserProfileDto profile = userService.getCurrentUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @Operation(
        summary = "Update current user profile",
        description = "Updates the authenticated user's profile. City is required and marks profile as completed."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or username already taken"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User or city not found")
    })
    public ResponseEntity<UserProfileDto> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileDto updateDto) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserProfileDto profile = userService.updateProfile(userId, updateDto);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get public user profile",
        description = "Returns limited public information about a user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<PublicUserProfileDto> getUserProfile(@PathVariable Long id) {
        PublicUserProfileDto profile = userService.getUserById(id);
        return ResponseEntity.ok(profile);
    }
}
