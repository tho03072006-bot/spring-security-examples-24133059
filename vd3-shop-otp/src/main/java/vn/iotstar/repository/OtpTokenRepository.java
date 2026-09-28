package vn.iotstar.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import vn.iotstar.entity.OtpToken;
public interface OtpTokenRepository extends JpaRepository<OtpToken,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpToken> findTopByEmailAndTypeOrderByCreatedAtDescIdDesc(String email,String type);
    void deleteByEmailAndType(String email,String type);
    void deleteByEmail(String email);
}
