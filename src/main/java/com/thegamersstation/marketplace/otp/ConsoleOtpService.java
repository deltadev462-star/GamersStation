package com.thegamersstation.marketplace.otp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@Profile("!prod")
public class ConsoleOtpService extends AbstractOtpService {

    /**
     * Configurable test phone→code map for dev/test environments.
     * Example config: otp.test-phones.+966500000000=1111
     */
    @Value("#{${otp.test-phones:{}}}")
    private Map<String, String> testPhones = Collections.emptyMap();

    public ConsoleOtpService(OtpLogRepository otpLogRepository) {
        super(otpLogRepository);
    }

    @Override
    public void sendOtp(String phoneNumber, String ipAddress, String language) {
        log.info("Sending OTP to console for phone: {} in language: {}", phoneNumber, language);

        validatePhoneFormat(phoneNumber);
        validateOtpRequest(phoneNumber, ipAddress);

        String code = generateOtpCode();
        logOtpAttemptWithCode(phoneNumber, ipAddress, true, code);

        String messageBody = buildOtpMessage(code, language);
        log.info("OTP generated for {} (code masked: {}, expires in {} minutes)",
                phoneNumber, code, ttlMinutes);
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String code) {
        log.info("Verifying OTP for phone: {}", phoneNumber);

        if (code == null || code.isBlank()) {
            return false;
        }

        // Allow configured test phone→code pairs in non-prod
        String testCode = testPhones.get(phoneNumber);
        if (testCode != null && testCode.equals(code.trim())) {
            log.info("OTP verified via test-phone allowlist for phone: {}", phoneNumber);
            return true;
        }

        return verifyOtpFromDb(phoneNumber, code);
    }
}
