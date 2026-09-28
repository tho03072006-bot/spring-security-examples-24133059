package vn.iotstar.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import vn.iotstar.security.CustomUserDetails;
@Component public class CurrentAccount {
    public CustomUserDetails get(){
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !(auth.getPrincipal() instanceof CustomUserDetails u))throw new AccessDeniedException("Cần đăng nhập");
        return u;
    }
    public boolean admin(){return get().getRole().equals("ROLE_ADMIN");}
    public void checkOwner(Long id){if(!admin() && !get().getId().equals(id))throw new AccessDeniedException("Bạn chỉ được sửa/xóa sản phẩm của mình.");}
}
