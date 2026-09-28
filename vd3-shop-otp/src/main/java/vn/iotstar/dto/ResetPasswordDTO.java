package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class ResetPasswordDTO {
@NotBlank @Email private String email;
@NotBlank @Pattern(regexp="[0-9]{6}",message="OTP phải gồm 6 chữ số") private String otp;
@NotBlank @Size(min=6,max=72,message="Mật khẩu gồm 6-72 ký tự") private String password;
@NotBlank private String confirmPassword;
}
