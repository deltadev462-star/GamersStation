package com.thegamersstation.marketplace.auth;

import com.thegamersstation.marketplace.auth.dto.AuthResponseDto;
import com.thegamersstation.marketplace.auth.dto.OtpRequestDto;
import com.thegamersstation.marketplace.auth.dto.OtpResponseDto;
import com.thegamersstation.marketplace.auth.dto.OtpVerifyDto;
import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import com.thegamersstation.marketplace.common.validation.PhoneValidator;
import com.thegamersstation.marketplace.otp.OtpService;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import com.thegamersstation.marketplace.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;

    @Value("${otp.ttl-minutes}")
    private int otpTtlMinutes;

    @Value("${otp.resend-cooldown-seconds}")
    private int resendCooldownSeconds;

    /**
     * Request OTP for phone number
     */
    public OtpResponseDto requestOtp(OtpRequestDto request, String ipAddress) {
        String phoneNumber = PhoneValidator.normalize(request.getPhoneNumber());
        
        if (phoneNumber == null) {
            throw new BusinessRuleException("Invalid phone number format");
        }

        log.info("OTP requested for phone: {}", phoneNumber);

        otpService.sendOtp(phoneNumber, ipAddress);

        return OtpResponseDto.builder()
                .message("OTP sent successfully")
                .expiresIn(otpTtlMinutes * 60) // Convert minutes to seconds
                .resendAfter(resendCooldownSeconds)
                .build();
    }

    /**
     * Verify OTP and authenticate user
     */
    @Transactional
    public AuthResponseDto verifyOtp(OtpVerifyDto request) {
        String phoneNumber = PhoneValidator.normalize(request.getPhoneNumber());
        
        if (phoneNumber == null) {
            throw new BusinessRuleException("Invalid phone number format");
        }

        log.info("OTP verification attempt for phone: {}", phoneNumber);

        // Verify OTP
        boolean isValid = otpService.verifyOtp(phoneNumber, request.getCode());
        
        if (!isValid) {
            throw new BusinessRuleException("Invalid or expired OTP code");
        }

        // Find or create user
        User user = usersRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> createNewUser(phoneNumber));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BusinessRuleException("Account is deactivated. Please contact support.");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), 
                user.getPhoneNumber(), 
                user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("User authenticated successfully: {} (isNew: {})", phoneNumber, user.getId() == null);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000) // Convert to seconds
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .profileCompleted(user.getProfileCompleted())
                .isNewUser(!user.getProfileCompleted())
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponseDto refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessRuleException("Refresh token is required");
        }

        // Validate refresh token
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BusinessRuleException("Invalid or expired refresh token");
        }

        // Extract user ID from refresh token
        Long userId = jwtUtil.extractUserId(refreshToken);

        // Find user
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BusinessRuleException("Account is deactivated");
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(), 
                user.getPhoneNumber(), 
                user.getRole().name()
        );

        // Optionally generate new refresh token (token rotation)
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("Token refreshed for user: {}", userId);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .profileCompleted(user.getProfileCompleted())
                .build();
    }

    /**
     * Create a new user on first login
     */
    private User createNewUser(String phoneNumber) {
        log.info("Creating new user for phone: {}", phoneNumber);

        User newUser = User.builder()
                .phoneNumber(phoneNumber)
                .role(User.UserRole.USER)
                .isActive(true)
                .profileCompleted(false)
                .build();

        return usersRepository.save(newUser);
    }
}
