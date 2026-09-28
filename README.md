# Bài tập Spring Security - Ví dụ 1, 2, 3

Thực hiện theo `vd1.pdf`, `vd2.pdf`, `vd3.pdf` trong thư mục tài liệu ngày 28/09/2026.
Ba ứng dụng độc lập nằm trong một Maven multi-module project.

Repository: [spring-security-examples-24133059](https://github.com/tho03072006-bot/spring-security-examples-24133059).

## Yêu cầu môi trường

- JDK **26**.
- Maven **3.9.16** (hoặc Maven >= 3.6.3).
- Spring Boot **4.1.1**, Spring Security **7.1.x**, MapStruct **1.6.3**, Thymeleaf Extras Spring Security 6.
- SQL Server cho chế độ `sqlserver`; SMTP và Cloudinary cho ví dụ 3.
- Chế độ `demo` dùng H2 lưu file, thư OTP lưu tại máy và ảnh lưu tại máy để có thể chạy thử khi chưa có tài khoản dịch vụ. Chế độ này được ghi rõ, không thay thế cấu hình SQL Server/SMTP/Cloudinary của bài.

## Cấu trúc và yêu cầu đã triển khai

| Thư mục | Yêu cầu | Cổng mặc định |
|---|---|---:|
| `vd1-email-login` | User/Role, đăng nhập bằng email, BCrypt, session, thông tin user trên header, MapStruct, Thymeleaf fragments **không dùng Layout Dialect** | 8081 |
| `vd2-custom-login` | User/Role, custom login bằng username hoặc email, principal riêng, fullname/ảnh/email/role trên header, MapStruct, **Thymeleaf Layout Dialect** | 8082 |
| `vd3-shop-otp` | Users/Roles/OtpToken/Products, quan hệ 1-n, OTP qua mail, đăng ký/kích hoạt/gửi lại OTP, login/logout/session, quên mật khẩu, CRUD user/product, tìm kiếm/phân trang, đếm user/product, upload/thay/xóa ảnh Cloudinary | 8083 |

Các lớp chính nằm trong `vn.iotstar`: `entity`, `dto`, `mapper`, `repository`, `security`, `config`, `controller`, `service`, `service.impl`.
Ví dụ 3 có interface service và lớp triển khai riêng. MapStruct sinh mapper khi Maven compile, không chuyển DTO thủ công thay cho mapper.
Header đọc thông tin user từ principal `CustomUserDetails` bằng `sec:authorize` và `${#authentication.principal}` như trong PDF.

## Build và chạy nhanh trên Windows

Có thể nhấp đúp `Chay_VD1_Demo.cmd`, `Chay_VD2_Demo.cmd`, `Chay_VD3_Demo.cmd` để chạy từng ví dụ sau khi build. Hoặc mở PowerShell trong thư mục này:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 -Example 1 -Profile demo
```

Mỗi ứng dụng chạy trong một cửa sổ terminal riêng. Đổi `-Example` thành `2` hoặc `3` để chạy ví dụ tương ứng.
Nhấn `Ctrl+C` ở cửa sổ chạy để dừng ứng dụng.

Hoặc dùng Maven khi `JAVA_HOME` đã trỏ đến JDK 26:

```powershell
./mvnw.cmd clean verify
cd vd3-shop-otp
java -jar target/vd3-shop-otp-1.0.0.jar --spring.profiles.active=demo
```

Mở `http://localhost:8081`, `http://localhost:8082`, `http://localhost:8083`.

| Tài khoản | Username | Email | Mật khẩu | Vai trò |
|---|---|---|---|---|
| Admin mẫu | admin | admin@iotstar.vn | 123456 | ADMIN |
| User mẫu | user01 | user01@gmail.com | 123456 | USER |
| User chưa kích hoạt | pending | pending@iotstar.vn | 123456 | USER, bị chặn login |

Ví dụ 1 nhập **email**. Ví dụ 2 nhập **username hoặc email**. Ví dụ 3 nhập **username**, theo mẫu `vd3.pdf`.
User mới do admin tạo có mật khẩu `123456` như tài liệu mẫu.
Dữ liệu mẫu được tạo một lần; khởi động lại không ghi đè mật khẩu hoặc dữ liệu đã chỉnh sửa.

## SQL Server, gửi mail và Cloudinary thật

1. Khởi động dịch vụ SQL Server trong SQL Server Configuration Manager hoặc Windows Services.
2. Chạy `sql/create-databases.sql` bằng SSMS để tạo **ba database riêng**: `security_vd1`, `security_vd2`, `security_vd3`.
3. Copy `.env.example` thành `.env` ở thư mục gốc bài làm. Điền URL instance/port, tài khoản DB, SMTP và Cloudinary của bạn. `.env` đã được loại khỏi Git bằng `.gitignore`.
4. Chạy:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 -Example 3 -Profile sqlserver
```

Khi chạy trực tiếp trong IDE mà không chọn profile, mặc định ứng dụng dùng **sqlserver**.
IDE cần đặt working directory ở thư mục module hoặc thư mục gốc bài làm để đọc `.env`.
JPA tạo các bảng khi khởi động; initializer tạo `ROLE_USER`, `ROLE_ADMIN` và tài khoản mẫu.
Họ tên, tên và mô tả sản phẩm dùng cột Unicode để lưu tiếng Việt trên SQL Server.

SMTP Gmail cần app password của tài khoản đã bật xác minh hai bước. Điền `MAIL_USERNAME` và `MAIL_PASSWORD` của bạn; không dùng giá trị minh họa trong PDF.
Cloudinary cần `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` lấy từ tài khoản của bạn.
Ảnh được upload bằng SDK, lưu riêng URL và public ID; sửa ảnh mới sẽ xóa ảnh cũ, xóa sản phẩm sẽ xóa ảnh trên cloud.
Cloudinary thật đã được kiểm tra riêng qua service: upload, truy cập ảnh HTTPS, thay ảnh và xóa ảnh đều thành công. Xem phạm vi xác minh trong `docs/KET_QUA_KIEM_THU.md`; cấu hình riêng lưu trong `.env` local.

## Kiểm tra OTP trong chế độ demo

Ví dụ 3 lưu thư tại **`vd3-shop-otp/data/mailbox/<email>.txt`** khi chạy bằng `scripts/run.ps1`.
Đây là hộp thư local của chế độ demo. Trong profile `sqlserver`, thư được gửi bằng Spring Mail tới địa chỉ đăng ký.

1. Đăng ký bằng username/email mới, nhập mật khẩu và xác nhận giống nhau.
2. Tài khoản chưa được đăng nhập cho đến khi xác nhận OTP 6 số.
3. Mở thư demo hoặc email thật, nhập OTP để kích hoạt.
4. Dùng chức năng quên mật khẩu để nhận OTP đặt lại mật khẩu.
5. OTP hết hạn sau **5 phút**, chỉ dùng **một lần**, tối đa **5 lần thử**. Gửi lại cách nhau tối thiểu **60 giây**; token cũ bị vô hiệu hóa.
6. OTP đăng ký và OTP đặt lại mật khẩu có mục đích riêng; không dùng thay thế nhau.

## Quản lý user và sản phẩm

- Admin được vào `/users`, thêm/sửa/xóa user, đổi role và trạng thái kích hoạt, tìm theo username/email/họ tên, chọn số dòng mỗi trang.
- Mỗi dòng user hiển thị số sản phẩm của user. Dashboard hiển thị tổng user, tổng sản phẩm và sản phẩm của tài khoản hiện tại.
- User được quản lý sản phẩm thuộc tài khoản của mình. Admin được quản lý toàn bộ sản phẩm.
- Sản phẩm mới luôn gắn với user đang đăng nhập; dữ liệu `userId` gửi từ trình duyệt không được dùng để đổi chủ sở hữu.
- Tìm sản phẩm theo tên/mô tả, phân trang, thêm/sửa/xóa sản phẩm và ảnh.
- Chỉ nhận ảnh hợp lệ tối đa 10 MB. Bỏ trống file khi sửa sẽ giữ ảnh hiện tại.
- Xóa user sẽ xóa các sản phẩm liên quan. Không được tự xóa/tự khóa tài khoản admin đang đăng nhập hoặc loại bỏ admin cuối cùng.
- Ba ứng dụng dùng cookie session riêng để có thể chạy đồng thời trên localhost. Các form POST dùng CSRF token do Thymeleaf/Spring Security cung cấp. Logout dùng POST và hủy session.

## Import vào Spring Tools / Eclipse

Chọn **File > Import > Maven > Existing Maven Projects**, trỏ đến thư mục bài làm và chọn cả parent cùng ba module.
Chọn JDK 26 cho project. Maven compile sẽ xử lý Lombok và MapStruct.
Chạy `vn.iotstar.Application` ở từng module; chọn profile `demo` nếu chưa cấu hình dịch vụ thật.

## Kiểm thử

`mvn verify` chạy kiểm thử tích hợp với H2 và dịch vụ mail/ảnh local. Không gửi email thật từ test.
Test kiểm tra đăng nhập, principal trên header, tài khoản chưa kích hoạt, CSRF, phân quyền, logout/session, validation, OTP hết hạn/số lần thử/gửi lại/dùng một lần, khôi phục mật khẩu, CRUD, tìm kiếm/phân trang, ảnh và xóa quan hệ user-product.
Xem kết quả từng test trong `*/target/surefire-reports/` và báo cáo `docs/KET_QUA_KIEM_THU.md`.

## Đối chiếu tài liệu và điều chỉnh để chạy được

- PDF `vd1.pdf` chứa cả phần giới thiệu ví dụ 2 và 3; phần triển khai tương ứng được tách thành các module riêng.
- Giữ Spring Boot 4.1.1/JDK 26/MapStruct và kiến trúc đề bài. Sửa các tham chiếu class không tồn tại, mapping và route không thống nhất trong đoạn mẫu.
- Đăng nhập USER không bị chuyển sang một URL chỉ cho ADMIN.
- Role thống nhất `ROLE_USER`/`ROLE_ADMIN`; `hasRole` dùng `USER`/`ADMIN`.
- Tải quan hệ cần thiết trong transaction, tắt Open Session in View; tránh lỗi lazy loading khi đưa dữ liệu ra view.
- DTO không chứa mật khẩu đã băm; ID, mật khẩu và thời gian tạo không được ghi đè từ form user.
- Không sao chép mật khẩu SMTP hay API secret minh họa trong PDF vào bài làm.

Nguồn API đối chiếu: [Spring Boot 4.1.1](https://docs.spring.io/spring-boot/system-requirements.html), [Spring Security DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html), [Cloudinary Java upload](https://cloudinary.com/documentation/java_image_and_video_upload).
