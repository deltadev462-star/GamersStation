package com.thegamersstation.marketplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "OTP request response")
public class OtpResponseDto {

    @Schema(description = "Success message", example = "OTP sent successfully")
    private String message;

    @Schema(description = "Seconds until OTP expires", example = "300")
    private Integer expiresIn;

    @Schema(description = "Seconds until can resend OTP", example = "60")
    private Integer resendAfter;
}
