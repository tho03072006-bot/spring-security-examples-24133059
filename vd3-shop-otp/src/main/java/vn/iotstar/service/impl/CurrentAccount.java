package vn.iotstar.service.impl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.iotstar.security.CustomUserDetails;

// Lấy tài khoản đang đăng nhập từ SecurityContext và kiểm tra quyền sở hữu
@Component
public class CurrentAccount {

    public CustomUserDetails get() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new AccessDeniedException("Cần đăng nhập");
        }
        return user;
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(get().getRole());
    }

    // Admin sửa được mọi sản phẩm; user chỉ sửa sản phẩm của mình
    public void checkOwner(Long ownerId) {
        if (!isAdmin() && !get().getId().equals(ownerId)) {
            throw new AccessDeniedException("Bạn chỉ được sửa/xóa sản phẩm của mình.");
        }
    }
}
