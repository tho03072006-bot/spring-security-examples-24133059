package vn.iotstar.service;

public interface OtpService {
void sendRegisterOtp(String email); void sendResetPasswordOtp(String email); boolean verifyRegisterOtp(String email,String otp); boolean verifyResetPasswordOtp(String email,String otp);
}
