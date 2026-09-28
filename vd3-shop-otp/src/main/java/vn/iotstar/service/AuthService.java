package vn.iotstar.service;

import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;

public interface AuthService {

    // Tạo tài khoản chưa kích hoạt và gửi OTP đăng ký
    void register(RegisterDTO dto);

    boolean verifyRegister(String email, String otp);

    void resendRegisterOtp(String email);

    // Gửi OTP đặt lại mật khẩu
    void forgotPassword(String email);

    boolean resetPassword(ResetPasswordDTO dto);
}
