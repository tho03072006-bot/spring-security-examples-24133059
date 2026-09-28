package vn.iotstar.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import vn.iotstar.entity.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // Khóa dòng khi đọc để hai request cùng lúc không vượt quá số lần thử
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpToken> findTopByEmailAndTypeOrderByCreatedAtDescIdDesc(String email, String type);

    void deleteByEmailAndType(String email, String type);

    void deleteByEmail(String email);
}
