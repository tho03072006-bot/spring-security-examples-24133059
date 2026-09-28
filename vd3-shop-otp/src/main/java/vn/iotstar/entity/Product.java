package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="products") @Getter @Setter @NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Nationalized @Column(nullable=false,length=500) private String name;
    @Nationalized @Column(length=5000) private String description;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal price;
    @Column(length=1000) private String imageUrl;
    @Column(length=500) private String imagePublicId;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
}
