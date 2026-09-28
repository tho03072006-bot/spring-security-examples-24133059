package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

// Gửi OTP qua SMTP thật (profile sqlserver)
@Service
@Profile("!demo & !test")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendOtp(String email, String otp, String subject) {
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(subject);
        message.setText("""
                Xin chào,
                Mã OTP của bạn là: %s
                Mã có hiệu lực trong 5 phút và chỉ sử dụng một lần.""".formatted(otp));
        mailSender.send(message);
    }
}
