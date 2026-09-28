package vn.iotstar.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ilike giữ tìm kiếm không phân biệt hoa/thường cả với nvarchar(max) trên SQL Server.
    // owner = null nghĩa là lấy sản phẩm của mọi user (admin).
    @EntityGraph(attributePaths = "user")
    @Query("""
            select p from Product p
            where (:owner is null or p.user.id = :owner)
              and (p.name ilike concat('%', :keyword, '%')
                   or p.description ilike concat('%', :keyword, '%'))
            """)
    Page<Product> search(@Param("keyword") String keyword, @Param("owner") Long owner, Pageable pageable);

    long countByUserId(Long userId);

    List<Product> findByUserId(Long userId);
}
