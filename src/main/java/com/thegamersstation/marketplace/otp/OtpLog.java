package com.thegamersstation.marketplace.otp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "otp_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;
}
