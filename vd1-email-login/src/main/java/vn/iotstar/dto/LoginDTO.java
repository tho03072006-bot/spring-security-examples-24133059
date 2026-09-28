package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Dữ liệu form login; POST /login do Spring Security xử lý
@Data
public class LoginDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
