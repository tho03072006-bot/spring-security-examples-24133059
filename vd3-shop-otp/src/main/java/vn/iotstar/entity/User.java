package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDateTime;
@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=50) private String username;
    @Column(nullable=false,unique=true,length=150) private String email;
    @Column(nullable=false,length=100) private String password;
    @Nationalized @Column(nullable=false,length=150) private String fullName;
    @Column(length=1000) private String images;
    @Column(nullable=false) private boolean enabled;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="role_id",nullable=false) private Role role;
    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, orphanRemoval=true)
    private java.util.List<Product> products = new java.util.ArrayList<>();
}
