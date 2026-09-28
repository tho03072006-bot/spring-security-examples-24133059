# Kết quả kiểm thử

Ngày kiểm tra: 28/09/2026.

Lệnh: `mvn clean verify` với JDK 26.0.2.1, Maven 3.9.16.
Kết quả: **BUILD SUCCESS**, 41 test, 0 failure, 0 error, 0 skipped.

| Module | Test suite | Số test | Failure | Error |
|---|---|---:|---:|---:|
| vd1-email-login | vn.iotstar.LoginIntegrationTest | 8 | 0 | 0 |
| vd2-custom-login | vn.iotstar.LoginIntegrationTest | 9 | 0 | 0 |
| vd3-shop-otp | vn.iotstar.LoginIntegrationTest | 8 | 0 | 0 |
| vd3-shop-otp | vn.iotstar.ShopIntegrationTest | 16 | 0 | 0 |

## Các luồng được kiểm tra tự động

- Ví dụ 1: login email, BCrypt, session và thông tin header, sai mật khẩu, user chưa kích hoạt, CSRF, logout, quyền ADMIN.
- Ví dụ 2: các kiểm tra trên và login bằng email hoặc username, kể cả khác chữ hoa/chữ thường; layout Dialect hiển thị được.
- Ví dụ 3: đăng ký và xác thực OTP; OTP hết hạn, dùng một lần, giới hạn 5 lần thử, cooldown gửi lại, token cũ bị thay; tách mục đích đăng ký/reset; đổi mật khẩu và BCrypt; validation form; CRUD user/product; sửa user giữ nguyên ảnh đại diện; tìm kiếm/phân trang; đếm sản phẩm; upload/thay/xóa ảnh local; kiểm tra quyền sở hữu; xóa user cùng sản phẩm; chống trùng username/email và tự xóa admin.

## Kiểm tra chạy ứng dụng và trình duyệt

- Các JAR chạy bằng JDK 26 với profile demo. Ví dụ 1 đã login bằng email; ví dụ 2 đã login bằng email và hiển thị họ tên/ảnh USER trên header; ba cookie session riêng không ghi đè nhau.
- Ví dụ 3: đăng nhập admin bằng form trên trình duyệt, header hiển thị fullname/email/ảnh/ROLE_ADMIN; xem danh sách user; tạo sản phẩm mẫu **Điện thoại Oppo A95**, giá **6,500,000.00**, chủ sở hữu **admin**.
- Có ảnh minh chứng trong `docs/screenshots/`.

## Phạm vi xác minh

Test dùng H2 in-memory; demo dùng H2 lưu file, thư OTP local và ảnh local.
Profile SQL Server, Spring Mail SMTP và Cloudinary SDK đã được triển khai riêng theo đề. Chưa xác minh kết nối dịch vụ thật: các SQL Server service đang dừng, tài khoản thực thi không có quyền khởi động service; chưa được cung cấp thông số DB/SMTP/Cloudinary của người dùng.
Không sử dụng các secret minh họa trong PDF.
