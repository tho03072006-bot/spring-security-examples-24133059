package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class LoginDTO {
    @NotBlank private String login;
    @NotBlank private String password;
}
