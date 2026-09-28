package vn.iotstar.dto;

import java.time.LocalDateTime;
import lombok.Data;

// Thông tin user đưa ra view, không chứa mật khẩu
@Data
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String images;
    private String roleName;
    private boolean enabled;
    private LocalDateTime createdAt;
}
