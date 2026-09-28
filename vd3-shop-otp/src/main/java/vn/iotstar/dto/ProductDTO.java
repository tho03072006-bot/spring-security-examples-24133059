package vn.iotstar.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm không được vượt quá 500 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5.000 ký tự")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", message = "Giá không được nhỏ hơn 0 đồng")
    @Digits(integer = 16, fraction = 2, message = "Giá tối đa 16 chữ số và 2 chữ số thập phân")
    private BigDecimal price;

    // Chỉ để hiển thị; ảnh mới được upload qua tham số "image" riêng
    private String imageUrl;
    private Long userId;
    private String username;
}
