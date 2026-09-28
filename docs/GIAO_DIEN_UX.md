# Giao diện và trải nghiệm sử dụng

Giao diện được thiết kế lại đồng bộ cho ba ví dụ ngày 28/09/2026: sidebar tím than, nền sáng và màu tím cho thao tác chính. Dashboard có vùng giới thiệu nổi bật, thẻ số liệu thực tế và lối tắt quản lý; trang đăng nhập có bố cục hai phần với minh họa sản phẩm SVG thiết kế riêng. Font hệ thống, CSS, JavaScript, icon và minh họa đều nằm trong project, không cần CDN.

## Nguồn tham khảo và cách áp dụng

| Nguồn | Áp dụng trong bài |
|---|---|
| [Atlassian Design: Spacing](https://atlassian.design/foundations/spacing) | Tham khảo nhịp khoảng cách và nhóm nội dung để phân biệt điều hướng, số liệu và thao tác. |
| [Atlassian Design: Navigation layout](https://atlassian.design/components/navigation-system/layout/code) | Tham khảo cấu trúc sidebar, thanh phía trên và vùng nội dung; trên điện thoại chuyển thành điều hướng ngang gọn. |
| [GOV.UK: Password input](https://design-system.service.gov.uk/components/password-input/) | Nút hiện/ẩn mật khẩu có nhãn truy cập, không chặn dán, hỗ trợ trình quản lý mật khẩu; chỉ hiện nút khi JavaScript hoạt động. |
| [GOV.UK: Pagination](https://design-system.service.gov.uk/components/pagination/) | Trang hiện tại có `aria-current`, nút trước/sau, cửa sổ số trang và dấu ba chấm; giữ từ khóa và số dòng khi chuyển trang. |
| [W3C WAI: Labeling controls](https://www.w3.org/WAI/tutorials/forms/labels/) | Nhãn luôn hiển thị và liên kết với trường nhập; gợi ý và lỗi liên kết qua `aria-describedby`. |
| [W3C WAI: User notifications](https://www.w3.org/WAI/tutorials/forms/notifications/) | Lỗi tổng hợp nhận focus sau khi gửi form; lỗi từng trường bằng tiếng Việt, `aria-invalid`; thông báo thành công có `role="status"`. |

Các nguồn được dùng để tham khảo hành vi và khả năng truy cập. Bố cục, màu sắc và đồ họa của giao diện được thiết kế riêng cho bài.

## Đối chiếu yêu cầu về giao diện

Trong các tài liệu được cung cấp không thấy yêu cầu giao diện phải giống hệt hình hoặc CSS mẫu. Việc thiết kế lại được hiểu là thay phần trình bày, đồng thời giữ các yêu cầu chức năng và kỹ thuật:

- `vd1.pdf`, trang 1: login với User/Role, thông tin user ở `header.html`, Spring Boot 4, Spring Security, MapStruct, Thymeleaf và layout không dùng Dialect. Module ví dụ 1 tiếp tục dùng fragments.
- `vd2.pdf`, trang 1: custom login bằng username hoặc email, fullname và images ở `header.html`. Module ví dụ 2 giữ cách ghép trang bằng Layout Dialect như mã mẫu.
- `vd3.pdf`, trang 1–3: các luồng đăng ký/OTP, login/session, reset password, CRUD/search/pagination/count, quan hệ User–Product và Cloudinary; view Thymeleaf, SQL Server và các công nghệ đã chỉ định.

Đây là cách đối chiếu từ tài liệu đã gửi, không phải một quy định cho phép thay đổi các yêu cầu kỹ thuật. Mẫu hiện tại vẫn dùng các template Thymeleaf của từng ví dụ, với CSS và đồ họa riêng.

## Những thay đổi chính

- Sidebar có icon, mục hiện tại được đánh dấu, thông tin principal và nút đăng xuất; thanh phía trên hiển thị tên màn hình hiện tại. Trên điện thoại, điều hướng chuyển thành các mục ngang dễ chạm.
- Minh họa SVG riêng dùng chung trên landing, dashboard và trang xác thực. Các thẻ thống kê chỉ hiển thị dữ liệu thật; họa tiết trang trí không mô phỏng biểu đồ tăng trưởng.
- Nhãn và nội dung phụ dùng cỡ chữ từ 12 px; trường nhập trên điện thoại dùng 16 px. Tăng độ tương phản của nội dung hướng dẫn để dễ đọc.
- Đợt trau chuốt tiếp theo giữ nguyên bố cục tím: tăng độ rõ của nhãn/bảng, làm mềm bóng thẻ, thêm nền cho liên kết chính trong banner và đồng bộ ảnh đại diện mẫu với màu của ứng dụng. Hiệu ứng hover ngắn và tiếp tục tắt khi người dùng yêu cầu giảm chuyển động.
- Đăng nhập có hiện/ẩn mật khẩu, autocomplete và thông báo trạng thái. Tài khoản mẫu nằm trong phần có thể mở rộng.
- Đăng ký có tiến trình ba bước. OTP dùng một ô 6 chữ số, bàn phím số, autocomplete, hướng dẫn hạn dùng và gửi lại.
- Dashboard có số liệu thực tế, nút thêm sản phẩm và lối tắt quản lý. Tài khoản thành viên thấy sản phẩm của mình; quản trị viên thấy số liệu toàn hệ thống.
- Bảng có nhãn tìm kiếm, chọn số dòng, trạng thái tài khoản dễ đọc, tổng kết kết quả và phân trang gọn. Không có dữ liệu và không tìm thấy kết quả có hướng dẫn tiếp theo.
- Form sản phẩm có đếm ký tự mô tả, xem trước ảnh, bỏ ảnh vừa chọn và kiểm tra định dạng/dung lượng ở trình duyệt. Server tiếp tục kiểm tra nội dung ảnh và giới hạn 10 MB.
- Khi gửi form POST hợp lệ, nút hiện trạng thái đang xử lý và chặn gửi lặp. Khôi phục nút khi quay lại bằng lịch sử trình duyệt.
- Xóa cần xác nhận có tên bản ghi và hậu quả; hộp thoại nhận focus ở nút “Giữ lại”, có thể đóng bằng Escape và trả focus về nút ban đầu.
- Có liên kết bỏ qua điều hướng, focus dễ thấy và hỗ trợ giảm chuyển động. Trên điện thoại, form dùng một cột; bảng cuộn ngang trong vùng riêng.

Ví dụ 1 và 3 tiếp tục ghép layout bằng Thymeleaf fragments. Ví dụ 2 tiếp tục dùng Thymeleaf Layout Dialect. Các đường dẫn, tên trường login, CSRF, session, quyền sở hữu sản phẩm, SQL Server, SMTP và Cloudinary giữ nguyên hợp đồng chức năng.

## Xác minh

- `mvn clean verify`: 45 test đạt; gồm ba test mới bảo đảm `/js/app.js` được tải khi chưa đăng nhập.
- Trình duyệt với các ứng dụng dùng SQL Server: login email ở ví dụ 1, login email ở ví dụ 2, login username/admin ở ví dụ 3; kiểm tra header và logout.
- Kiểm tra hiện/ẩn mật khẩu, tìm kiếm không có kết quả, phân trang với `size=1`, mở/hủy xác nhận xóa và focus mặc định.
- Kiểm tra chọn/bỏ ảnh xem trước, đếm ký tự và phản hồi validation khi giá vượt giới hạn; bản ghi không được tạo từ dữ liệu không hợp lệ.
- Kiểm tra form trên viewport 320, 390 và 768 px; các màn hình đã kiểm tra không tràn ngang trang. Bảng giữ vùng cuộn ngang riêng.
- Lần kiểm tra giao diện không gửi thêm email OTP hoặc thay đổi mật khẩu/tài khoản mẫu.

Đây là kiểm tra chức năng và bố cục trên trình duyệt của máy hiện tại, chưa phải chứng nhận đầy đủ WCAG hoặc kiểm thử trên mọi thiết bị.

## Ảnh minh chứng

![Đăng nhập trên máy tính](screenshots/ui-login-desktop.png)

![Dashboard](screenshots/ui-dashboard-desktop.png)

![Quản lý người dùng](screenshots/ui-users-desktop.png)

| Đăng nhập trên điện thoại | Form sản phẩm trên điện thoại |
|---|---|
| ![Đăng nhập](screenshots/ui-login-mobile.png) | ![Form sản phẩm](screenshots/ui-product-form-mobile.png) |

Hai ví dụ đăng nhập: [Ví dụ 1](screenshots/ui-vd1-account.png), [Ví dụ 2](screenshots/ui-vd2-account.png).
