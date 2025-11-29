package com.thegamersstation.marketplace.auth;

import com.thegamersstation.marketplace.auth.dto.AuthResponseDto;
import com.thegamersstation.marketplace.auth.dto.OtpRequestDto;
import com.thegamersstation.marketplace.auth.dto.OtpResponseDto;
import com.thegamersstation.marketplace.auth.dto.OtpVerifyDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP-based authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/request")
    @Operation(
        summary = "Request OTP",
        description = "Send OTP code to phone number. Rate limited to prevent abuse.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "OTP sent successfully",
                content = @Content(schema = @Schema(implementation = OtpResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid phone number or rate limit exceeded"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
        }
    )
    public ResponseEntity<OtpResponseDto> requestOtp(
            @Valid @RequestBody OtpRequestDto request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        OtpResponseDto response = authService.requestOtp(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    @Operation(
        summary = "Verify OTP and authenticate",
        description = "Verify OTP code and receive JWT tokens. Auto-creates user on first login.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Authentication successful",
                content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP code"),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
        }
    )
    public ResponseEntity<AuthResponseDto> verifyOtp(
            @Valid @RequestBody OtpVerifyDto request
    ) {
        AuthResponseDto response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Get new access token using refresh token. Implements token rotation.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
        }
    )
    public ResponseEntity<AuthResponseDto> refreshToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        String refreshToken = authHeader.replace("Bearer ", "");
        AuthResponseDto response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
