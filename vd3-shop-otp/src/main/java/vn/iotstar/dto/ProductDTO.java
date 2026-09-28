package vn.iotstar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class ProductDTO {
private Long id;
@NotBlank @Size(max=500) private String name;
@Size(max=5000) private String description;
@NotNull @DecimalMin("0.0") @Digits(integer=16,fraction=2) private java.math.BigDecimal price;
private String imageUrl;
private Long userId;
private String username;
}
