package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class UserDTO implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    private Long id;
    @NotBlank @Pattern(regexp="[a-zA-Z0-9_.-]{3,50}",message="Username gồm 3-50 ký tự chữ, số, dấu . _ -") private String username;
    @NotBlank @Email @Size(max=150) private String email;
    @NotBlank @Size(max=150) private String fullName;
    private String images;
    @NotBlank @Pattern(regexp="ROLE_(USER|ADMIN)",message="Vai trò không hợp lệ") private String roleName;
    private boolean enabled;
    private long productCount;
    private LocalDateTime createdAt;
}
