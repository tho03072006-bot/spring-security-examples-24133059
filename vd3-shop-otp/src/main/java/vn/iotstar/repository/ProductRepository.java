package vn.iotstar.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.Product;
public interface ProductRepository extends JpaRepository<Product,Long> {
    @EntityGraph(attributePaths="user")
    @Query("select p from Product p where (:owner is null or p.user.id=:owner) and (lower(p.name) like lower(concat('%',:keyword,'%')) or lower(coalesce(p.description,'')) like lower(concat('%',:keyword,'%')))")
    Page<Product> search(@Param("keyword") String keyword,@Param("owner") Long owner,Pageable pageable);
    long countByUserId(Long userId);
    java.util.List<Product> findByUserId(Long userId);
}
