package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Dữ liệu form login; ô "login" nhận username hoặc email
@Data
public class LoginDTO {

    @NotBlank(message = "Username hoặc email không được để trống")
    private String login;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
