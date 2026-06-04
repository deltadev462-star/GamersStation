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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${otp.ttl-minutes}")
    private int otpTtlMinutes;

    @Value("${otp.resend-cooldown-seconds}")
    private int resendCooldownSeconds;

    /**
     * Request OTP for phone number
     */
    public OtpResponseDto requestOtp(OtpRequestDto request, String ipAddress, String language) {
        String phoneNumber = PhoneValidator.normalize(request.getPhoneNumber());
        
        if (phoneNumber == null) {
            throw new BusinessRuleException(
                "Invalid phone number format",
                "صيغة رقم الجوال غير صحيحة"
            );
        }
        
        log.info("OTP requested for phone: {} with language: {}", phoneNumber, language);

        otpService.sendOtp(phoneNumber, ipAddress, language);

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
            throw new BusinessRuleException(
                "Invalid phone number format",
                "صيغة رقم الجوال غير صحيحة"
            );
        }

        log.info("OTP verification attempt for phone: {}", phoneNumber);

        // Verify OTP
        boolean isValid = otpService.verifyOtp(phoneNumber, request.getCode());
        
        if (!isValid) {
            throw new BusinessRuleException(
                "Invalid or expired OTP code",
                "رمز التحقق غير صحيح أو منتهي الصلاحية"
            );
        }

        // Find or create user
        User user = usersRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> createNewUser(phoneNumber));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BusinessRuleException(
                "Account is deactivated. Please contact support.",
                "حسابك غير مفعّل. يرجى التواصل مع الدعم الفني."
            );
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), 
                user.getPhoneNumber(), 
                user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Store refresh token with a new family (new login session)
        storeRefreshToken(user.getId(), refreshToken, UUID.randomUUID().toString());

        log.info("User authenticated successfully: {} (isNew: {})", phoneNumber, user.getId() == null);

        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();

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
                .hasEmail(hasEmail)
                .build();
    }

    /**
     * Refresh access token using refresh token.
     * Implements token rotation with family-based theft detection:
     * - Each refresh token is single-use (consumed after one rotation).
     * - If a consumed token is replayed, the entire family is revoked.
     */
    @Transactional
    public AuthResponseDto refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessRuleException(
                "Refresh token is required",
                "رمز التحديث مطلوب"
            );
        }

        // Validate JWT signature and expiry
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BusinessRuleException(
                "Invalid or expired refresh token",
                "رمز التحديث غير صحيح أو منتهي الصلاحية"
            );
        }

        // Look up the token in the database by its jti hash
        String jti = jwtUtil.extractJti(refreshToken);
        if (jti == null) {
            throw new BusinessRuleException(
                "Invalid refresh token format",
                "صيغة رمز التحديث غير صحيحة"
            );
        }

        String jtiHash = hashJti(jti);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(jtiHash)
                .orElseThrow(() -> new BusinessRuleException(
                    "Refresh token not recognized",
                    "رمز التحديث غير معروف"
                ));

        // THEFT DETECTION: if the token was already consumed, an attacker is replaying it.
        // Revoke the entire family to protect the user.
        if (storedToken.getIsConsumed()) {
            refreshTokenRepository.revokeFamily(storedToken.getTokenFamily());
            log.warn("Refresh token reuse detected for user {}. Family {} revoked.",
                    storedToken.getUserId(), storedToken.getTokenFamily());
            throw new BusinessRuleException(
                "Suspicious activity detected. Please log in again.",
                "تم اكتشاف نشاط مشبوه. يرجى تسجيل الدخول مرة أخرى."
            );
        }

        // If the token or its family has been revoked, reject.
        if (storedToken.getIsRevoked()) {
            throw new BusinessRuleException(
                "Refresh token has been revoked",
                "تم إلغاء رمز التحديث"
            );
        }

        // Mark current token as consumed (single-use)
        storedToken.setIsConsumed(true);
        refreshTokenRepository.save(storedToken);

        // Validate user
        Long userId = jwtUtil.extractUserId(refreshToken);
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException(
                    "User not found",
                    "المستخدم غير موجود"
                ));

        if (!user.getIsActive()) {
            refreshTokenRepository.revokeFamily(storedToken.getTokenFamily());
            throw new BusinessRuleException(
                "Account is deactivated",
                "حسابك غير مفعّل"
            );
        }

        // Issue new tokens (rotation) in the SAME family
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getPhoneNumber(),
                user.getRole().name()
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Store the new refresh token in the same family
        storeRefreshToken(user.getId(), newRefreshToken, storedToken.getTokenFamily());

        log.info("Token refreshed for user: {}", userId);

        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .profileCompleted(user.getProfileCompleted())
                .hasEmail(hasEmail)
                .build();
    }

    /**
     * Logout user by revoking their refresh token family.
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return; // Silently ignore — user is already effectively logged out
        }

        try {
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                return; // Token already expired or invalid — nothing to revoke
            }

            String jti = jwtUtil.extractJti(refreshToken);
            if (jti == null) {
                return;
            }

            String jtiHash = hashJti(jti);
            refreshTokenRepository.findByTokenHash(jtiHash)
                    .ifPresent(storedToken -> {
                        refreshTokenRepository.revokeFamily(storedToken.getTokenFamily());
                        log.info("User {} logged out. Family {} revoked.",
                                storedToken.getUserId(), storedToken.getTokenFamily());
                    });
        } catch (Exception e) {
            log.warn("Error during logout token revocation: {}", e.getMessage());
            // Don't throw — logout should always succeed from the client's perspective
        }
    }

    /**
     * Persist a refresh token record for revocation tracking.
     */
    private void storeRefreshToken(Long userId, String jwt, String family) {
        String jti = jwtUtil.extractJti(jwt);
        Instant expiresAt = jwtUtil.extractExpiration(jwt).toInstant();

        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .tokenFamily(family)
                .tokenHash(hashJti(jti))
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(entity);
    }

    /**
     * SHA-256 hash of a jti value for safe storage.
     */
    private String hashJti(String jti) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(jti.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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
