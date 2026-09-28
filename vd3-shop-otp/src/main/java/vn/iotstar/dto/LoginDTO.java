package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Dữ liệu form login; POST /login do Spring Security xử lý
@Data
public class LoginDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
