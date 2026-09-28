package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class RegisterDTO {
@NotBlank @Pattern(regexp="[a-zA-Z0-9_.-]{3,50}",message="Username gồm 3-50 ký tự chữ, số, dấu . _ -") private String username;
@NotBlank @Email @Size(max=150) private String email;
@NotBlank @Size(max=150) private String fullName;
@NotBlank @Size(min=6,max=72,message="Mật khẩu gồm 6-72 ký tự") private String password;
@NotBlank private String confirmPassword;
}
