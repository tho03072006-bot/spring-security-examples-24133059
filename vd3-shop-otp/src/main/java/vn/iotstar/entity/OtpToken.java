package vn.iotstar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Mỗi email chỉ giữ một OTP còn hiệu lực cho mỗi loại (REGISTER / RESET_PASSWORD)
@Entity
@Table(name = "otp_tokens", indexes = @Index(name = "idx_otp_email_type", columnList = "email,type"))
@Getter
@Setter
@NoArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    // Chỉ lưu bản băm BCrypt của OTP, không lưu mã gốc
    @Column(nullable = false, length = 100)
    private String otpHash;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Số lần nhập sai
    private int attempts;

    private boolean used;
}
