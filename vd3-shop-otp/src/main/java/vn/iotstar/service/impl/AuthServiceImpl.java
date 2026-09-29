package vn.iotstar.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AccountSessionService accountSessions;

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        requireMatchingPasswords(dto.getPassword(), dto.getConfirmPassword());
        String username = normalize(dto.getUsername());
        String email = normalize(dto.getEmail());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username đã tồn tại.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        // Tài khoản mới bị khóa cho tới khi xác nhận OTP
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(dto.getFullName().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_USER").orElseThrow());
        user.setEnabled(false);
        user.setImages("/images/avatar.svg");
        userRepository.save(user);

        otpService.sendRegisterOtp(email);
    }

    @Override
    @Transactional
    public boolean verifyRegister(String email, String otp) {
        var user = userRepository.findLockedByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || user.isEnabled() || !otpService.verifyRegisterOtp(email, otp)) {
            return false;
        }
        user.setEnabled(true);
        return true;
    }

    @Override
    @Transactional
    public void resendRegisterOtp(String email) {
        var user = userRepository.findLockedByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        if (user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản đã được kích hoạt.");
        }
        otpService.sendRegisterOtp(user.getEmail());
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        var user = userRepository.findLockedByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại."));
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt.");
        }
        otpService.sendResetPasswordOtp(user.getEmail());
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordDTO dto) {
        requireMatchingPasswords(dto.getPassword(), dto.getConfirmPassword());
        var user = userRepository.findLockedByEmailIgnoreCase(dto.getEmail().trim()).orElse(null);
        if (user == null || !user.isEnabled() || !otpService.verifyResetPasswordOtp(dto.getEmail(), dto.getOtp())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        accountSessions.expireAfterCommit(user.getId());
        return true;
    }

    private static void requireMatchingPasswords(String password, String confirmPassword) {
        if (password == null || password.isBlank() || password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu quá dài khi chứa ký tự có dấu hoặc biểu tượng. Hãy rút ngắn mật khẩu.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
