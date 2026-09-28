package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Dữ liệu form quản lý user, không chứa mật khẩu
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "Username không được để trống")
    @Pattern(regexp = "[a-zA-Z0-9_.-]{3,50}", message = "Username gồm 3-50 ký tự chữ, số, dấu . _ -")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150)
    private String fullName;

    private String images;

    @NotBlank
    @Pattern(regexp = "ROLE_(USER|ADMIN)", message = "Vai trò không hợp lệ")
    private String roleName;

    private boolean enabled;

    // Số sản phẩm của user, service tự tính
    private long productCount;
}
