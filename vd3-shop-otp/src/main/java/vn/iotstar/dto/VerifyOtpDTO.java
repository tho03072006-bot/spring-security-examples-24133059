package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "[0-9]{6}", message = "OTP phải gồm 6 chữ số")
    private String otp;
}
