package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import vn.iotstar.service.EmailService;
@Service @Profile("!demo & !test") @RequiredArgsConstructor
public class SmtpEmailService implements EmailService {
    private final JavaMailSender sender;
    @Value("${spring.mail.username}") private String from;
    public void sendOtp(String email,String otp,String subject){
        var message=new SimpleMailMessage(); message.setFrom(from);message.setTo(email);message.setSubject(subject);
        message.setText("Xin chào,\nMã OTP của bạn là: "+otp+"\nMã có hiệu lực trong 5 phút và chỉ sử dụng một lần.");
        sender.send(message);
    }
}
