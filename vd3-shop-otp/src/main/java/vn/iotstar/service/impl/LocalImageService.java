package vn.iotstar.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

// Profile demo/test: lưu ảnh vào thư mục uploads/ thay cho Cloudinary
@Service
@Profile({"demo", "test"})
public class LocalImageService implements CloudinaryService {

    private static final Path ROOT = Path.of("uploads").toAbsolutePath().normalize();

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        byte[] bytes = ImageValidation.readImage(file);
        String id = UUID.randomUUID() + ".png";
        try {
            Files.createDirectories(ROOT);
            Files.write(ROOT.resolve(id), bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Không lưu được ảnh demo", e);
        }
        return new CloudinaryUploadResult("/uploads/" + id, id);
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        // Chặn đường dẫn kiểu ../ thoát ra ngoài thư mục uploads
        Path file = ROOT.resolve(publicId).normalize();
        if (!file.startsWith(ROOT)) {
            throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ");
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException("Không xóa được ảnh demo", e);
        }
    }
}
