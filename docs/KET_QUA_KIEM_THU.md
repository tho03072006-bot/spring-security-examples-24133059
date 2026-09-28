# Kết quả kiểm thử

Ngày kiểm tra: 28/09/2026.

Lệnh: `mvn clean verify` với JDK 26.0.2.1, Maven 3.9.16.
Kết quả: **BUILD SUCCESS**, 42 test, 0 failure, 0 error, 0 skipped. Lần build gần nhất hoàn tất lúc 10:25 ngày 28/09/2026.

| Module | Test suite | Số test | Failure | Error |
|---|---|---:|---:|---:|
| vd1-email-login | vn.iotstar.LoginIntegrationTest | 8 | 0 | 0 |
| vd2-custom-login | vn.iotstar.LoginIntegrationTest | 9 | 0 | 0 |
| vd3-shop-otp | vn.iotstar.LoginIntegrationTest | 8 | 0 | 0 |
| vd3-shop-otp | vn.iotstar.ShopIntegrationTest | 17 | 0 | 0 |

## Các luồng được kiểm tra tự động

- Ví dụ 1: login email, BCrypt, session và thông tin header, sai mật khẩu, user chưa kích hoạt, CSRF, logout, quyền ADMIN.
- Ví dụ 2: các kiểm tra trên và login bằng email hoặc username, kể cả khác chữ hoa/chữ thường; layout Dialect hiển thị được.
- Ví dụ 3: đăng ký và xác thực OTP; OTP hết hạn, dùng một lần, giới hạn 5 lần thử, cooldown gửi lại, token cũ bị thay; tách mục đích đăng ký/reset; đổi mật khẩu và BCrypt; validation form; CRUD user/product; sửa user giữ nguyên ảnh đại diện; tìm kiếm/phân trang; đếm sản phẩm; upload/thay/xóa ảnh local; kiểm tra quyền sở hữu; xóa user cùng sản phẩm; chống trùng username/email và tự xóa admin.
- Kiểm tra tìm sản phẩm theo từ khóa tiếng Việt ở cuối mô tả dài hơn 4.000 ký tự, khác chữ hoa/thường; sản phẩm có mô tả null vẫn tìm được theo tên.

## Kiểm tra chạy ứng dụng và trình duyệt

- Các JAR chạy bằng JDK 26 với profile demo. Ví dụ 1 đã login bằng email; ví dụ 2 đã login bằng email và hiển thị họ tên/ảnh USER trên header; ba cookie session riêng không ghi đè nhau.
- Ví dụ 3: đăng nhập admin bằng form trên trình duyệt, header hiển thị fullname/email/ảnh/ROLE_ADMIN; xem danh sách user; tạo sản phẩm mẫu **Điện thoại Oppo A95**, giá **6,500,000.00**, chủ sở hữu **admin**.
- Có ảnh minh chứng trong `docs/screenshots/`.

## Kiểm thử dịch vụ thật qua HTTP

Ba ứng dụng dùng profile `sqlserver`, kết nối `localhost:1433` bằng SQL Server Authentication. Kiểm thử bằng HTTP client với cookie session riêng, CSRF token lấy từ form HTML do Thymeleaf render và các controller của ứng dụng.

| Hạng mục | Kết quả |
|---|---|
| Ví dụ 1 | Login email trên SQL Server, session/header, từ chối tài khoản chưa kích hoạt: đạt |
| Ví dụ 2 | Login username và email trên SQL Server, header/Layout Dialect: đạt |
| Ví dụ 3: login | Login admin, dashboard và danh sách user: đạt |
| User CRUD | Thêm, đọc, sửa, xóa qua HTTP; họ tên tiếng Việt lưu đúng; tìm kiếm/phân trang: đạt |
| Product CRUD | Thêm, đọc, sửa, xóa trên SQL Server; dữ liệu userId từ form không đổi chủ sở hữu; tìm kiếm/phân trang: đạt |
| Mô tả dài | Tìm từ khóa khác chữ hoa/thường ở cuối mô tả Unicode dài hơn 4.000 ký tự trên `nvarchar(max)`: đạt |
| Cloudinary | Upload ảnh qua form multipart, lưu URL/public ID trong SQL Server, thay ảnh và xóa ảnh cũ, xóa sản phẩm cùng ảnh mới: đạt |
| Đăng ký/OTP | SMTP chấp nhận email đăng ký; user bị khóa trước OTP; xác nhận OTP kích hoạt trong SQL Server; mã chỉ dùng một lần; USER bị chặn trang ADMIN: đạt |
| Quên mật khẩu | SMTP chấp nhận email reset; OTP đặt mật khẩu mới trong SQL Server; mật khẩu cũ bị từ chối, mật khẩu mới login được; logout: đạt |

SMTP thật xác thực thành công bằng App Password lấy từ cấu hình local của project đã có theo yêu cầu người dùng. Hai email OTP được gửi tới hộp thư của tài khoản SMTP. Helper kiểm thử giữ mã trong bộ nhớ sau khi `SmtpEmailService` gửi thành công để thực hiện POST xác nhận và reset; kết quả gửi mail xác nhận SMTP chấp nhận thư, chưa đối chiếu thư trong giao diện inbox.

Tất cả user/product/OTP và ảnh Cloudinary tạo trong kiểm thử đã được dọn sạch. Sau kiểm thử, mỗi database giữ 3 user mẫu và 2 role; `security_vd3` có 0 product. Chỉ thao tác trên ba database riêng của bài tập.

## Sửa lỗi phát hiện trên SQL Server

Khi mô tả sản phẩm dài 5.000 ký tự được ánh xạ thành `nvarchar(max)`, Hibernate kiểm tra HQL `lower(coalesce(p.description, ''))` như kiểu NCLOB và từ chối khởi động ứng dụng. Đã đổi truy vấn sản phẩm sang `ilike`, giữ tìm kiếm không phân biệt hoa/thường và kiểm thử trên cả H2 lẫn SQL Server thật.

## Phạm vi xác minh

Test dùng H2 in-memory; demo dùng H2 lưu file, thư OTP local và ảnh local.
Profile SQL Server, Spring Mail SMTP và Cloudinary SDK đã được triển khai riêng theo đề.

- **Cloudinary thật: đã xác minh.** Gọi trực tiếp `CloudinaryServiceImpl` đã build cùng Cloudinary SDK của ví dụ 3: upload PNG hợp lệ, đọc URL HTTPS và giải mã ảnh thành công; upload ảnh thay thế, xóa ảnh cũ, xóa ảnh mới. Kiểm tra lại bằng SDK xác nhận cả hai ảnh không còn tồn tại. Sau kiểm tra service độc lập, luồng CRUD qua HTTP kết hợp SQL Server và Cloudinary cũng đã đạt như bảng trên. Tất cả ảnh thử đã được dọn sạch.
- **SQL Server thật: đã xác minh.** Đã tạo `security_vd1`, `security_vd2`, `security_vd3`, chạy cả ba ứng dụng với SQL login và kiểm thử các luồng HTTP nêu trên.
- **SMTP thật: đã xác minh xác thực và gửi thư.** Hai email đăng ký/reset được SMTP chấp nhận; các bước xác nhận OTP và đặt lại mật khẩu đã đạt trên SQL Server.

Thông tin SQL Server, SMTP và Cloudinary chỉ lưu trong `.env` local, không đưa lên Git. Bộ 42 test tự động dùng H2 và dịch vụ mail/ảnh local; các kiểm tra dịch vụ thật được thực hiện riêng.
Không sử dụng các secret minh họa trong PDF.
