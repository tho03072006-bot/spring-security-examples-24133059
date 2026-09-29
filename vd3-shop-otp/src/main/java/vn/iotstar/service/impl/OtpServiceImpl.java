package vn.iotstar.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.OtpService;

// OTP 6 số: hết hạn sau 5 phút, dùng một lần, tối đa 5 lần nhập sai, gửi lại cách nhau 60 giây
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final String REGISTER = "REGISTER";
    private static final String RESET_PASSWORD = "RESET_PASSWORD";
    private static final int EXPIRE_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_SECONDS = 60;

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void sendRegisterOtp(String email) {
        send(email, REGISTER, "IOTSTAR SHOP - Xác nhận đăng ký");
    }

    @Override
    @Transactional
    public void sendResetPasswordOtp(String email) {
        send(email, RESET_PASSWORD, "IOTSTAR SHOP - Đặt lại mật khẩu");
    }

    @Override
    @Transactional
    public boolean verifyRegisterOtp(String email, String otp) {
        return verify(email, otp, REGISTER);
    }

    @Override
    @Transactional
    public boolean verifyResetPasswordOtp(String email, String otp) {
        return verify(email, otp, RESET_PASSWORD);
    }

    private void send(String email, String type, String subject) {
        String address = email.trim().toLowerCase(Locale.ROOT);
        userRepository.findLockedByEmailIgnoreCase(address)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        var previous = otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDescIdDesc(address, type).orElse(null);
        if (previous != null && previous.getCreatedAt().plusSeconds(RESEND_SECONDS).isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Vui lòng chờ 60 giây trước khi gửi lại OTP.");
        }

        // Xóa OTP cũ cùng loại để mã cũ không dùng được nữa
        otpTokenRepository.deleteByEmailAndType(address, type);
        otpTokenRepository.flush();

        String code = "%06d".formatted(random.nextInt(1_000_000));
        var token = new OtpToken();
        token.setEmail(address);
        token.setType(type);
        token.setOtpHash(passwordEncoder.encode(code));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        otpTokenRepository.save(token);

        emailService.sendOtp(address, code, subject);
    }

    private boolean verify(String email, String code, String type) {
        String address = email.trim().toLowerCase(Locale.ROOT);
        // Thứ tự khóa luôn là User rồi OTP, tránh gửi lại và xác thực khóa chéo nhau.
        if (userRepository.findLockedByEmailIgnoreCase(address).isEmpty()) {
            return false;
        }
        var token = otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDescIdDesc(address, type).orElse(null);
        if (token == null
                || token.isUsed()
                || !token.getExpiresAt().isAfter(LocalDateTime.now())
                || token.getAttempts() >= MAX_ATTEMPTS) {
            return false;
        }

        // Tăng số lần thử trước khi so khớp; entity được lưu khi transaction commit
        token.setAttempts(token.getAttempts() + 1);
        if (code == null || !passwordEncoder.matches(code, token.getOtpHash())) {
            return false;
        }
        token.setUsed(true);
        return true;
    }
}
