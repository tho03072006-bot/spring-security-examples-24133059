# Bài tập Spring Security — Ví dụ 1, 2, 3

- Sinh viên: **Trần Minh Thọ**
- MSSV: **24133059**
- Môn: Lập trình Web — bài tập ngày 28/09/2026 (ví dụ 1, 2, 3 trong tài liệu "Hướng dẫn chức năng login bằng Spring Security 7")

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

Trên Linux/macOS, build bằng `./mvnw clean verify`, sau đó chạy ví dụ 3:

```sh
cd vd3-shop-otp
java -jar target/vd3-shop-otp-1.0.0.jar --spring.profiles.active=demo
```

## Đối chiếu backend với đề bài

| Yêu cầu | Mã nguồn / kiểm thử |
|---|---|
| VD1: email, thông tin header, fragments không Layout Dialect | [Module VD1](vd1-email-login/src), [LoginIntegrationTest](vd1-email-login/src/test/java/vn/iotstar/LoginIntegrationTest.java) |
| VD2: username hoặc email, fullname/images, Layout Dialect | [Module VD2](vd2-custom-login/src), [LoginIntegrationTest](vd2-custom-login/src/test/java/vn/iotstar/LoginIntegrationTest.java) |
| VD3: đăng ký, xác nhận/gửi lại OTP, khôi phục mật khẩu | [AuthServiceImpl và OtpServiceImpl](vd3-shop-otp/src/main/java/vn/iotstar/service/impl), [ShopIntegrationTest](vd3-shop-otp/src/test/java/vn/iotstar/ShopIntegrationTest.java) |
| CRUD, tìm kiếm, phân trang, đếm user/product, quan hệ 1–n | [Service](vd3-shop-otp/src/main/java/vn/iotstar/service/impl), [Entity](vd3-shop-otp/src/main/java/vn/iotstar/entity), [MapStruct](vd3-shop-otp/src/main/java/vn/iotstar/mapper) |
| Session, BCrypt, phân quyền, quyền sở hữu, OTP đồng thời, rollback ảnh | [BackendIntegrityIntegrationTest](vd3-shop-otp/src/test/java/vn/iotstar/BackendIntegrityIntegrationTest.java) |
| Xử lý lỗi SMTP và upload Cloudinary | [ExternalServiceFailureIntegrationTest](vd3-shop-otp/src/test/java/vn/iotstar/ExternalServiceFailureIntegrationTest.java) |

`clean verify` chạy kiểm thử cả ba module. Profile `test` dùng H2 cùng mail/ảnh local; riêng kiểm thử lỗi dịch vụ ngoài sử dụng mock. Profile `sqlserver` dùng SQL Server, SMTP và Cloudinary thật. Khi khóa/xóa/đổi thông tin đăng nhập hoặc đặt lại mật khẩu, phiên cũ bị thu hồi sau khi lưu thành công.

## Tài khoản mẫu

| Username | Email | Họ tên | Vai trò |
|---|---|---|---|
| `admin` | `admin@iotstar.vn` | Quản trị viên | ADMIN |
| `user01` | `user01@gmail.com` | Trần Minh Thọ | USER |
| `pending` | `pending@iotstar.vn` | Tài khoản chưa kích hoạt | USER, bị chặn đăng nhập |

Mật khẩu mẫu: `123456`. Ví dụ 1 nhập email, ví dụ 2 nhập username hoặc email, ví dụ 3 nhập username.

`.env`, dữ liệu local và kết quả build được bỏ qua bởi Git. Không commit mật khẩu hoặc API secret.
