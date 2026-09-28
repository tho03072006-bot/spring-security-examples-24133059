package vn.iotstar.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

// Profile demo/test: ghi OTP ra data/mailbox/<email>.txt thay vì gửi mail thật
@Service
@Profile({"demo", "test"})
public class DemoEmailService implements EmailService {

    private static final Path MAILBOX = Path.of("data", "mailbox");

    @Override
    public void sendOtp(String email, String otp, String subject) {
        String fileName = email.replaceAll("[^a-zA-Z0-9@._-]", "_") + ".txt";
        String content = subject + "\nEmail: " + email + "\nOTP: " + otp + "\nHiệu lực: 5 phút";
        try {
            Files.createDirectories(MAILBOX);
            Files.writeString(MAILBOX.resolve(fileName), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Không thể lưu thư demo", e);
        }
    }
}
