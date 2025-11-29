package com.thegamersstation.marketplace.otp;

/**
 * Service interface for OTP operations
 */
public interface OtpService {
    
    /**
     * Send OTP to phone number
     * @param phoneNumber Phone number in E.164 format (+966...)
     * @param ipAddress IP address of the requester
     */
    void sendOtp(String phoneNumber, String ipAddress);
    
    /**
     * Verify OTP code
     * @param phoneNumber Phone number in E.164 format
     * @param code OTP code entered by user
     * @return true if verification successful, false otherwise
     */
    boolean verifyOtp(String phoneNumber, String code);
    
    /**
     * Check if phone number can request OTP (rate limiting check)
     * @param phoneNumber Phone number to check
     * @param ipAddress IP address to check
     */
    void validateOtpRequest(String phoneNumber, String ipAddress);
}
