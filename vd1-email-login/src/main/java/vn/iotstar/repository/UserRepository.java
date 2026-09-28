package vn.iotstar.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Nạp luôn role để tạo authority khi đăng nhập
    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}
