package vn.iotstar.repository;

import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.User;
public interface UserRepository extends JpaRepository<User,Long> {
    @EntityGraph(attributePaths="role") Optional<User> findByEmailIgnoreCase(String email);
    @EntityGraph(attributePaths="role") Optional<User> findByUsernameIgnoreCase(String username);
    @EntityGraph(attributePaths="role") Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username,String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    long countByRoleNameAndEnabledTrue(String roleName);
    @EntityGraph(attributePaths="role")
    @Query("select u from User u where lower(u.username) like lower(concat('%',:keyword,'%')) or lower(u.email) like lower(concat('%',:keyword,'%')) or lower(u.fullName) like lower(concat('%',:keyword,'%'))")
    Page<User> search(@Param("keyword") String keyword,Pageable pageable);
}
