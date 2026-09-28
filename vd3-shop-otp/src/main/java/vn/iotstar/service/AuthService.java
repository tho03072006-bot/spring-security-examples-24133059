package vn.iotstar.service;

public interface AuthService {
void register(vn.iotstar.dto.RegisterDTO dto); boolean verifyRegister(String email,String otp); void resendRegisterOtp(String email); void forgotPassword(String email); boolean resetPassword(vn.iotstar.dto.ResetPasswordDTO dto);
}
