# Bài tập Spring Security — Ví dụ 1, 2, 3

Spring Boot 4.1.1, Spring Security 7, MapStruct và Thymeleaf. Mỗi module là một ứng dụng độc lập.

| Module | Chức năng | Cổng |
|---|---|---:|
| `vd1-email-login` | Login bằng email, Thymeleaf fragments | 8081 |
| `vd2-custom-login` | Login bằng username/email, Layout Dialect | 8082 |
| `vd3-shop-otp` | Đăng ký/OTP, khôi phục mật khẩu, quản lý user và sản phẩm, Cloudinary | 8083 |

## Chạy ứng dụng

Yêu cầu **JDK 26**. Với profile `sqlserver`:

1. Chạy `sql/create-databases.sql` trên SQL Server.
2. Copy `.env.example` thành `.env`, điền kết nối SQL Server, SMTP và Cloudinary.
3. Build và chạy từ thư mục gốc:

```powershell
./mvnw.cmd clean verify
powershell -NoProfile -ExecutionPolicy Bypass -File ./scripts/run.ps1 -Example 3 -Profile sqlserver
```

Đổi `-Example` thành `1`, `2` hoặc `3`. Mở `http://localhost:8081`, `8082` hoặc `8083` tương ứng. Dừng bằng `Ctrl+C`.

Để chạy thử với H2, mail và ảnh lưu tại máy, dùng `-Profile demo`. Thư OTP demo nằm trong `<module>/data/mailbox/`.

## Tài khoản mẫu

| Username | Email | Vai trò |
|---|---|---|
| `admin` | `admin@iotstar.vn` | ADMIN |
| `user01` | `user01@gmail.com` | USER |

Mật khẩu mẫu: `123456`. Ví dụ 1 nhập email, ví dụ 2 nhập username hoặc email, ví dụ 3 nhập username.

`.env`, dữ liệu local và kết quả build được bỏ qua bởi Git. Không commit mật khẩu hoặc API secret.
