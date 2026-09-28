package vn.iotstar.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.*;
@Service @RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService email;
    private final SecureRandom random=new SecureRandom();
    private void send(String address,String type,String subject){
        address=address.trim().toLowerCase(java.util.Locale.ROOT);
        var previous=tokens.findTopByEmailAndTypeOrderByCreatedAtDescIdDesc(address,type).orElse(null);
        if(previous!=null && previous.getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now()))throw new IllegalArgumentException("Vui lòng chờ 60 giây trước khi gửi lại OTP.");
        tokens.deleteByEmailAndType(address,type);tokens.flush();
        String code="%06d".formatted(random.nextInt(1_000_000));
        var token=new OtpToken();token.setEmail(address);token.setType(type);token.setOtpHash(encoder.encode(code));token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        tokens.save(token);email.sendOtp(address,code,subject);
    }
    @Transactional public void sendRegisterOtp(String address){send(address,"REGISTER","IOTSTAR SHOP - Xác nhận đăng ký");}
    @Transactional public void sendResetPasswordOtp(String address){send(address,"RESET_PASSWORD","IOTSTAR SHOP - Đặt lại mật khẩu");}
    private boolean verify(String address,String code,String type){
        var token=tokens.findTopByEmailAndTypeOrderByCreatedAtDescIdDesc(address.trim().toLowerCase(java.util.Locale.ROOT),type).orElse(null);
        if(token==null || token.isUsed() || !token.getExpiresAt().isAfter(LocalDateTime.now()) || token.getAttempts()>=5)return false;
        token.setAttempts(token.getAttempts()+1);
        if(code==null || !encoder.matches(code,token.getOtpHash()))return false;
        token.setUsed(true);return true;
    }
    @Transactional public boolean verifyRegisterOtp(String address,String code){return verify(address,code,"REGISTER");}
    @Transactional public boolean verifyResetPasswordOtp(String address,String code){return verify(address,code,"RESET_PASSWORD");}
}
