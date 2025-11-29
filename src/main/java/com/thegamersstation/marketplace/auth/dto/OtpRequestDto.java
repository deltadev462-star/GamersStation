package com.thegamersstation.marketplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP request payload")
public class OtpRequestDto {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+966[0-9]{9}$", message = "Phone number must be in E.164 format (+966XXXXXXXXX)")
    @Schema(description = "Phone number in E.164 format", example = "+966501234567", required = true)
    private String phoneNumber;
}
