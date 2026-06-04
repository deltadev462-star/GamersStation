package com.thegamersstation.marketplace.otp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Profile("prod")
public class SmsOtpService extends AbstractOtpService {

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${otp.sms.api-url}")
    private String apiUrl;

    @Value("${otp.sms.auth-token}")
    private String authToken;

    @Value("${otp.sms.sender}")
    private String sender;

    public SmsOtpService(OtpLogRepository otpLogRepository) {
        super(otpLogRepository);
    }

    @Override
    public void sendOtp(String phoneNumber, String ipAddress, String language) {
        log.info("Sending OTP via SMS to phone: {}", phoneNumber);

        validatePhoneFormat(phoneNumber);
        validateOtpRequest(phoneNumber, ipAddress);

        String code = generateOtpCode();

        boolean success = sendSms(phoneNumber, code, language);
        logOtpAttemptWithCode(phoneNumber, ipAddress, success, code);

        if (!success) {
            throw new BusinessRuleException(
                "Failed to send OTP. Please try again.",
                "فشل إرسال رمز التحقق. يرجى المحاولة مرة أخرى."
            );
        }

        log.info("OTP sent successfully to {} (expires in {} minutes)", phoneNumber, ttlMinutes);
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String code) {
        log.info("Verifying OTP for phone: {}", phoneNumber);
        return verifyOtpFromDb(phoneNumber, code);
    }

    private boolean sendSms(String phoneNumber, String code, String language) {
        try {
            String formattedPhone = phoneNumber.replace("+", "");
            String messageBody = buildOtpMessage(code, language);

            Map<String, Object> payload = Map.of(
                    "sender", sender,
                    "recipients", List.of(formattedPhone),
                    "body", messageBody
            );

            String jsonPayload = objectMapper.writeValueAsString(payload);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(jsonPayload, MediaType.get("application/json")))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authToken)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("SMS sent successfully to {}", phoneNumber);
                    return true;
                } else {
                    log.error("SMS API error: {} - {}", response.code(), response.body().string());
                    return false;
                }
            }

        } catch (IOException e) {
            log.error("Failed to send SMS", e);
            return false;
        }
    }
}
