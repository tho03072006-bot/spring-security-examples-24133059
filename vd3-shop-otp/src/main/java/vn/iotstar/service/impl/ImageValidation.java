package vn.iotstar.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.web.multipart.MultipartFile;

// Kiểm tra file upload thật sự là ảnh, không chỉ dựa vào đuôi file
public final class ImageValidation {

    private static final long MAX_SIZE = 10L * 1024 * 1024;

    private ImageValidation() {
    }

    public static byte[] readImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn ảnh");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Ảnh tối đa 10 MB");
        }
        try {
            byte[] bytes = file.getBytes();
            if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                throw new IllegalArgumentException("Chỉ chấp nhận ảnh PNG, JPEG, GIF hoặc BMP hợp lệ");
            }
            return bytes;
        } catch (IOException e) {
            throw new IllegalArgumentException("Không đọc được ảnh", e);
        }
    }
}
