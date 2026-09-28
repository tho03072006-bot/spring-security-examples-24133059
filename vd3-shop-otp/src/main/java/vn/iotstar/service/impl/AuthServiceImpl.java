package vn.iotstar.service.impl;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.User;
import vn.iotstar.dto.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.*;
@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final OtpService otp;
    @Transactional public void register(RegisterDTO dto){
        if(!dto.getPassword().equals(dto.getConfirmPassword()))throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        String username=dto.getUsername().trim().toLowerCase(Locale.ROOT),email=dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if(users.existsByUsernameIgnoreCase(username))throw new IllegalArgumentException("Username đã tồn tại.");
        if(users.existsByEmailIgnoreCase(email))throw new IllegalArgumentException("Email đã tồn tại.");
        var user=new User();user.setUsername(username);user.setEmail(email);user.setFullName(dto.getFullName().trim());
        user.setPassword(encoder.encode(dto.getPassword()));user.setRole(roles.findByName("ROLE_USER").orElseThrow());user.setEnabled(false);
        user.setImages("/images/avatar.svg");users.save(user);otp.sendRegisterOtp(email);
    }
    @Transactional public boolean verifyRegister(String email,String code){
        var user=users.findByEmailIgnoreCase(email.trim()).orElse(null);
        if(user==null || user.isEnabled() || !otp.verifyRegisterOtp(email,code))return false;
        user.setEnabled(true);return true;
    }
    @Transactional public void resendRegisterOtp(String email){
        var user=users.findByEmailIgnoreCase(email.trim()).orElseThrow(()->new IllegalArgumentException("Tài khoản không tồn tại."));
        if(user.isEnabled())throw new IllegalArgumentException("Tài khoản đã được kích hoạt.");
        otp.sendRegisterOtp(user.getEmail());
    }
    @Transactional public void forgotPassword(String email){
        var user=users.findByEmailIgnoreCase(email.trim()).orElseThrow(()->new IllegalArgumentException("Email không tồn tại."));
        if(!user.isEnabled())throw new IllegalArgumentException("Tài khoản chưa được kích hoạt.");
        otp.sendResetPasswordOtp(user.getEmail());
    }
    @Transactional public boolean resetPassword(ResetPasswordDTO dto){
        if(!dto.getPassword().equals(dto.getConfirmPassword()))throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        var user=users.findByEmailIgnoreCase(dto.getEmail().trim()).orElse(null);
        if(user==null || !user.isEnabled() || !otp.verifyResetPasswordOtp(dto.getEmail(),dto.getOtp()))return false;
        user.setPassword(encoder.encode(dto.getPassword()));return true;
    }
}
