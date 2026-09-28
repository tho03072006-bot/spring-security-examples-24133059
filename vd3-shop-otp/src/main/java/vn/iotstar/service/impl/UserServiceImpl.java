package vn.iotstar.service.impl;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.*;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;
import vn.iotstar.repository.*;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.service.*;
@Service @RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final ProductRepository products;
    private final OtpTokenRepository tokens;
    private final CloudinaryService images;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final CurrentAccount account;
    private User require(Long id){return users.findById(id).orElseThrow(()->new IllegalArgumentException("User không tồn tại."));}
    private UserDTO dto(User user){var dto=mapper.toDTO(user);dto.setProductCount(products.countByUserId(user.getId()));return dto;}
    @PreAuthorize("hasRole('ADMIN')") @Transactional(readOnly=true)
    public Page<UserDTO> findAll(String keyword,int page,int size){
        return users.search(keyword==null?"":keyword.trim(),PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size)),Sort.by(Sort.Direction.DESC,"id"))).map(this::dto);
    }
    @PreAuthorize("hasRole('ADMIN')") @Transactional(readOnly=true) public UserDTO findById(Long id){return dto(require(id));}
    private void validate(UserDTO dto,Long id){
        dto.setUsername(dto.getUsername().trim().toLowerCase(Locale.ROOT));dto.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));dto.setFullName(dto.getFullName().trim());
        users.findByUsernameIgnoreCase(dto.getUsername()).filter(u->!u.getId().equals(id)).ifPresent(u->{throw new IllegalArgumentException("Username đã tồn tại.");});
        users.findByEmailIgnoreCase(dto.getEmail()).filter(u->!u.getId().equals(id)).ifPresent(u->{throw new IllegalArgumentException("Email đã tồn tại.");});
    }
    @PreAuthorize("hasRole('ADMIN')") @Transactional public UserDTO create(UserDTO dto){
        validate(dto,null);var user=mapper.toEntity(dto);user.setRole(roles.findByName(dto.getRoleName()).orElseThrow(()->new IllegalArgumentException("Vai trò không hợp lệ")));
        user.setPassword(encoder.encode("123456"));user.setImages("/images/avatar.svg");return dto(users.save(user));
    }
    @PreAuthorize("hasRole('ADMIN')") @Transactional public UserDTO update(Long id,UserDTO dto){
        var user=require(id);validate(dto,id);
        boolean removesAdmin=user.isEnabled() && user.getRole().getName().equals("ROLE_ADMIN") && (!dto.isEnabled() || !dto.getRoleName().equals("ROLE_ADMIN"));
        if(removesAdmin && users.countByRoleNameAndEnabledTrue("ROLE_ADMIN")<=1)throw new IllegalArgumentException("Phải giữ ít nhất một quản trị viên đang hoạt động.");
        if(id.equals(account.get().getId()) && (!dto.isEnabled() || !dto.getRoleName().equals("ROLE_ADMIN")))throw new IllegalArgumentException("Không thể tự khóa hoặc hạ quyền tài khoản đang đăng nhập.");
        mapper.update(dto,user);user.setRole(roles.findByName(dto.getRoleName()).orElseThrow());return dto(user);
    }
    @PreAuthorize("hasRole('ADMIN')") @Transactional public void delete(Long id){
        var user=require(id);
        if(id.equals(account.get().getId()))throw new IllegalArgumentException("Không thể xóa tài khoản đang đăng nhập.");
        if(user.isEnabled() && user.getRole().getName().equals("ROLE_ADMIN") && users.countByRoleNameAndEnabledTrue("ROLE_ADMIN")<=1)throw new IllegalArgumentException("Không thể xóa quản trị viên cuối cùng.");
        for(var product:products.findByUserId(id))images.delete(product.getImagePublicId());
        tokens.deleteByEmail(user.getEmail());users.delete(user);
    }
    @Transactional(readOnly=true) public long countUsers(){return users.count();}
    @Transactional(readOnly=true) public long countProducts(Long id){return products.countByUserId(id);}
}
