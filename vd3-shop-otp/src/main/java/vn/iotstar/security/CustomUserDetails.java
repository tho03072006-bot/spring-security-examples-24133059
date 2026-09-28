package vn.iotstar.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.iotstar.dto.UserDTO;

// Principal lưu trong session; header và service đọc id, fullName, role từ đây
@Getter
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final String images;
    private final String role;
    private final boolean enabled;

    public CustomUserDetails(UserDTO user, String password) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = password;
        this.fullName = user.getFullName();
        this.images = user.getImages();
        this.role = user.getRoleName();
        this.enabled = user.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    // Tài khoản chưa xác nhận OTP sẽ bị từ chối đăng nhập
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // So sánh theo id để maximumSessions nhận ra cùng một tài khoản
    @Override
    public boolean equals(Object other) {
        return other instanceof CustomUserDetails user && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
