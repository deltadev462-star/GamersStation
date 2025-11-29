package com.thegamersstation.marketplace.admin.user;

import com.thegamersstation.marketplace.common.dto.PageRequestDto;
import com.thegamersstation.marketplace.common.dto.PageResponseDto;
import com.thegamersstation.marketplace.user.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Admin endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserModerationController {

    private final UserModerationService userModerationService;

    @GetMapping
    @Operation(
        summary = "Get all users (Admin)",
        description = "Returns paginated list of all users with complete profiles"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)")
    })
    public ResponseEntity<PageResponseDto<UserProfileDto>> getAllUsers(
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        PageResponseDto<UserProfileDto> users = userModerationService.getAllUsers(pageRequest);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/ban")
    @Operation(
        summary = "Ban user (Admin)",
        description = "Deactivates a user account by setting isActive to false"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User banned successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserProfileDto> banUser(@PathVariable Long id) {
        UserProfileDto user = userModerationService.banUser(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/unban")
    @Operation(
        summary = "Unban user (Admin)",
        description = "Reactivates a banned user account by setting isActive to true"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User unbanned successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (admin only)"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserProfileDto> unbanUser(@PathVariable Long id) {
        UserProfileDto user = userModerationService.unbanUser(id);
        return ResponseEntity.ok(user);
    }
}
