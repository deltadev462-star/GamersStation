package com.thegamersstation.marketplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP verification payload")
public class OtpVerifyDto {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+966[0-9]{9}$", message = "Phone number must be in E.164 format (+966XXXXXXXXX)")
    @Schema(description = "Phone number in E.164 format", example = "+966501234567", required = true)
    private String phoneNumber;

    @NotBlank(message = "OTP code is required")
    @Size(min = 4, max = 4, message = "OTP code must be 4 digits")
    @Pattern(regexp = "^[0-9]{4}$", message = "OTP code must contain only digits")
    @Schema(description = "4-digit OTP code", example = "1234", required = true)
    private String code;
}
