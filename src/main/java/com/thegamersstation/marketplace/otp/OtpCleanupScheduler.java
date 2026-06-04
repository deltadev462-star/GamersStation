package com.thegamersstation.marketplace.otp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled job to clean up expired OTP entries from the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpLogRepository otpLogRepository;

    /**
     * Run cleanup every hour to remove expired OTP entries.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredOtps() {
        int deleted = otpLogRepository.deleteExpiredOtps(Instant.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired OTP entries", deleted);
        }
    }
}
