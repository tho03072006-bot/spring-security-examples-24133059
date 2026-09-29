package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

// Tạo role và tài khoản mẫu; chỉ thêm khi chưa có nên không ghi đè dữ liệu cũ
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed:true}")
    private boolean seed;

    @Override
    @Transactional
    public void run(String... args) {
        Role userRole = findOrCreateRole("ROLE_USER");
        Role adminRole = findOrCreateRole("ROLE_ADMIN");
        if (!seed) {
            return;
        }
        addUser("admin", "admin@iotstar.vn", "Quản trị viên", adminRole, true);
        addUser("user01", "user01@gmail.com", "Trần Minh Thọ", userRole, true);
        addUser("pending", "pending@iotstar.vn", "Tài khoản chưa kích hoạt", userRole, false);
    }

    private Role findOrCreateRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private void addUser(String username, String email, String fullName, Role role, boolean enabled) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setEnabled(enabled);
        user.setImages("/images/avatar.svg");
        userRepository.save(user);
    }
}
