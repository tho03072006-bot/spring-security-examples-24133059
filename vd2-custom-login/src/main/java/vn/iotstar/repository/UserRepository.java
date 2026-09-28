package vn.iotstar.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm theo username hoặc email, nạp luôn role để tạo authority
    @EntityGraph(attributePaths = "role")
    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    boolean existsByUsernameIgnoreCase(String username);
}
