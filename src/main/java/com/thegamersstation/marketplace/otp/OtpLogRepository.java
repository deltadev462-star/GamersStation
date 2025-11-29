package com.thegamersstation.marketplace.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface OtpLogRepository extends JpaRepository<OtpLog, Long> {

    /**
     * Count OTP attempts for a phone number within a time range
     */
    long countByPhoneNumberAndAttemptedAtAfter(String phoneNumber, Instant after);

    /**
     * Count OTP attempts for an IP address within a time range
     */
    long countByIpAddressAndAttemptedAtAfter(String ipAddress, Instant after);

    /**
     * Get the last successful OTP attempt for a phone number
     */
    @Query("SELECT o FROM OtpLog o WHERE o.phoneNumber = :phone AND o.success = true ORDER BY o.attemptedAt DESC LIMIT 1")
    OtpLog findLastSuccessfulAttempt(@Param("phone") String phoneNumber);

    /**
     * Get the last OTP attempt (success or failure) for a phone number
     */
    @Query("SELECT o FROM OtpLog o WHERE o.phoneNumber = :phone ORDER BY o.attemptedAt DESC LIMIT 1")
    OtpLog findLastAttempt(@Param("phone") String phoneNumber);
}
