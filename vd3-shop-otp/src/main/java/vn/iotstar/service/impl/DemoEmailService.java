package vn.iotstar.service.impl;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import vn.iotstar.service.EmailService;
@Service @Profile({"demo","test"})
public class DemoEmailService implements EmailService {
    public void sendOtp(String email,String otp,String subject){
        try{
            Files.createDirectories(Path.of("data/mailbox"));
            String safe=email.replaceAll("[^a-zA-Z0-9@._-]","_");
            Files.writeString(Path.of("data/mailbox",safe+".txt"),subject+"\nEmail: "+email+"\nOTP: "+otp+"\nHiệu lực: 5 phút",StandardCharsets.UTF_8);
        }catch(java.io.IOException e){throw new IllegalStateException("Không thể lưu thư demo",e);}
    }
}
