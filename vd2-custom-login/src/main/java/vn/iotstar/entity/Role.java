package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lưu dạng ROLE_USER / ROLE_ADMIN để dùng trực tiếp làm authority
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
