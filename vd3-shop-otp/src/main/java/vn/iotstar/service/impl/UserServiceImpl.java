package vn.iotstar.service.impl;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final ImageCleanup imageCleanup;
    private final AccountSessionService accountSessions;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentAccount currentAccount;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(String keyword, int page, int size) {
        String text = keyword == null ? "" : keyword.trim();
        var pageable = PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100), Sort.by(Sort.Direction.DESC, "id"));
        return userRepository.search(text, pageable).map(this::toDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        return toDTO(requireUser(id));
    }

    // User do admin tạo dùng mật khẩu mặc định 123456
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDTO create(UserDTO dto) {
        normalizeAndCheckUnique(dto, null);
        User user = userMapper.toEntity(dto);
        user.setRole(roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ")));
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setImages("/images/avatar.svg");
        return toDTO(userRepository.save(user));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        User user = requireUserForUpdate(id);
        normalizeAndCheckUnique(dto, id);

        boolean keepsAdmin = dto.isEnabled() && ADMIN.equals(dto.getRoleName());
        boolean isActiveAdmin = user.isEnabled() && ADMIN.equals(user.getRole().getName());
        if (isActiveAdmin && !keepsAdmin && userRepository.countByRoleNameAndEnabledTrue(ADMIN) <= 1) {
            throw new IllegalArgumentException("Phải giữ ít nhất một quản trị viên đang hoạt động.");
        }
        if (id.equals(currentAccount.get().getId()) && !keepsAdmin) {
            throw new IllegalArgumentException("Không thể tự khóa hoặc hạ quyền tài khoản đang đăng nhập.");
        }

        boolean credentialsChanged = !user.getUsername().equals(dto.getUsername())
                || !user.getEmail().equals(dto.getEmail())
                || !user.getRole().getName().equals(dto.getRoleName())
                || user.isEnabled() != dto.isEnabled();
        if (credentialsChanged) {
            // OTP gửi tới địa chỉ cũ không được áp dụng cho tài khoản khác dùng lại email đó.
            otpTokenRepository.deleteByEmail(user.getEmail());
            accountSessions.expireAfterCommit(id);
        }
        userMapper.update(dto, user);
        user.setRole(roleRepository.findByName(dto.getRoleName()).orElseThrow());
        return toDTO(user);
    }

    // Xóa user kéo theo sản phẩm (cascade), ảnh sản phẩm và OTP của user
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        User user = requireUserForUpdate(id);
        if (id.equals(currentAccount.get().getId())) {
            throw new IllegalArgumentException("Không thể xóa tài khoản đang đăng nhập.");
        }
        boolean isActiveAdmin = user.isEnabled() && ADMIN.equals(user.getRole().getName());
        if (isActiveAdmin && userRepository.countByRoleNameAndEnabledTrue(ADMIN) <= 1) {
            throw new IllegalArgumentException("Không thể xóa quản trị viên cuối cùng.");
        }
        for (var product : productRepository.findByUserId(id)) {
            imageCleanup.afterCommit(product.getImagePublicId());
        }
        otpTokenRepository.deleteByEmail(user.getEmail());
        userRepository.delete(user);
        accountSessions.expireAfterCommit(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts(Long userId) {
        return productRepository.countByUserId(userId);
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại."));
    }

    private User requireUserForUpdate(Long id) {
        return userRepository.findLockedById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại."));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = userMapper.toDTO(user);
        dto.setProductCount(productRepository.countByUserId(user.getId()));
        return dto;
    }

    // Chuẩn hóa chữ thường, bỏ khoảng trắng; username/email không được trùng user khác
    private void normalizeAndCheckUnique(UserDTO dto, Long currentId) {
        dto.setUsername(dto.getUsername().trim().toLowerCase(Locale.ROOT));
        dto.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));
        dto.setFullName(dto.getFullName().trim());
        userRepository.findByUsernameIgnoreCase(dto.getUsername())
                .filter(other -> !other.getId().equals(currentId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Username đã tồn tại.");
                });
        userRepository.findByEmailIgnoreCase(dto.getEmail())
                .filter(other -> !other.getId().equals(currentId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Email đã tồn tại.");
                });
    }
}
