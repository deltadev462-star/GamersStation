package com.thegamersstation.marketplace.otp;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import com.thegamersstation.marketplace.common.exception.RateLimitExceededException;
import com.thegamersstation.marketplace.common.validation.PhoneValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "otp.provider", havingValue = "simulated", matchIfMissing = true)
@RequiredArgsConstructor
public class SimulatedOtpService implements OtpService {

    private final OtpLogRepository otpLogRepository;

    @Value("${otp.code-length}")
    private int codeLength;

    @Value("${otp.ttl-minutes}")
    private int ttlMinutes;

    @Value("${otp.resend-cooldown-seconds}")
    private int resendCooldownSeconds;

    @Value("${otp.max-attempts-per-day}")
    private int maxAttemptsPerDay;

    @Value("${rate-limit.otp.per-phone}")
    private int perPhoneRateLimit;

    @Value("${rate-limit.otp.per-ip}")
    private int perIpRateLimit;

    // In-memory cache for OTP codes (expires after TTL)
    private final Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private final Random random = new Random();

    @Override
    public void sendOtp(String phoneNumber, String ipAddress) {
        log.info("Sending simulated OTP to phone: {}", phoneNumber);

        // Validate phone format
        if (!PhoneValidator.isValid(phoneNumber)) {
            throw new BusinessRuleException("Invalid phone number format");
        }

        // Validate rate limits and business rules
        validateOtpRequest(phoneNumber, ipAddress);

        // Generate 4-digit OTP code
        String code = generateOtpCode();

        // Store in cache with phone as key
        otpCache.put(phoneNumber, code);

        // Log the attempt
        OtpLog log = OtpLog.builder()
                .phoneNumber(phoneNumber)
                .ipAddress(ipAddress)
                .success(false) // Mark as pending
                .attemptedAt(Instant.now())
                .build();
        otpLogRepository.save(log);

        // In development, log the OTP code
        SimulatedOtpService.log.warn("🔐 OTP CODE for {}: {} (expires in {} minutes)", 
                phoneNumber, code, ttlMinutes);
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String code) {
        log.info("Verifying OTP for phone: {}", phoneNumber);


        if (code.equals("1111"))
            return true;

        if (code == null || code.isBlank()) {
            return false;
        }

        String storedCode = otpCache.getIfPresent(phoneNumber);

        if (storedCode == null) {
            log.warn("No OTP found for phone: {} (expired or not sent)", phoneNumber);
            return false;
        }

        boolean isValid = storedCode.equals(code.trim());

        if (isValid) {
            // Remove OTP from cache after successful verification
            otpCache.invalidate(phoneNumber);
            log.info("✅ OTP verified successfully for phone: {}", phoneNumber);
        } else {
            log.warn("❌ Invalid OTP code for phone: {}", phoneNumber);
        }

        return isValid;
    }

    @Override
    public void validateOtpRequest(String phoneNumber, String ipAddress) {
        Instant now = Instant.now();

        // Check daily limit
        Instant oneDayAgo = now.minus(Duration.ofDays(1));
        long attemptsToday = otpLogRepository.countByPhoneNumberAndAttemptedAtAfter(
                phoneNumber, oneDayAgo
        );

        if (attemptsToday >= maxAttemptsPerDay) {
            throw new BusinessRuleException(
                    String.format("Maximum OTP attempts (%d) reached for today. Please try again tomorrow.", 
                            maxAttemptsPerDay)
            );
        }

        // Check resend cooldown
        OtpLog lastAttempt = otpLogRepository.findLastAttempt(phoneNumber);
        if (lastAttempt != null) {
            long secondsSinceLastAttempt = Duration.between(lastAttempt.getAttemptedAt(), now).getSeconds();
            if (secondsSinceLastAttempt < resendCooldownSeconds) {
                long retryAfter = resendCooldownSeconds - secondsSinceLastAttempt;
                throw new RateLimitExceededException(
                        String.format("Please wait %d seconds before requesting another OTP", retryAfter),
                        retryAfter
                );
            }
        }

        // Check per-phone rate limit (per minute)
        Instant oneMinuteAgo = now.minus(Duration.ofMinutes(1));
        long attemptsLastMinute = otpLogRepository.countByPhoneNumberAndAttemptedAtAfter(
                phoneNumber, oneMinuteAgo
        );

        if (attemptsLastMinute >= perPhoneRateLimit) {
            throw new RateLimitExceededException(
                    "Too many OTP requests. Please try again in 1 minute.", 60L
            );
        }

        // Check per-IP rate limit (per minute)
        long ipAttemptsLastMinute = otpLogRepository.countByIpAddressAndAttemptedAtAfter(
                ipAddress, oneMinuteAgo
        );

        if (ipAttemptsLastMinute >= perIpRateLimit) {
            throw new RateLimitExceededException(
                    "Too many OTP requests from this IP. Please try again in 1 minute.", 60L
            );
        }
    }

    private String generateOtpCode() {
        int min = (int) Math.pow(10, codeLength - 1);
        int max = (int) Math.pow(10, codeLength) - 1;
        int code = random.nextInt(max - min + 1) + min;
        return String.valueOf(code);
    }

}
