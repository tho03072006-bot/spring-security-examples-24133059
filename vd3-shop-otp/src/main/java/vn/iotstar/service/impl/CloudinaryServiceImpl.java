package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

// Upload/xóa ảnh trên Cloudinary bằng SDK (profile sqlserver)
@Service
@Profile("!demo & !test")
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        byte[] bytes = ImageValidation.readImage(file);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(bytes,
                    Map.of("folder", "security-assignment/products", "resource_type", "image"));
            return new CloudinaryUploadResult(
                    result.get("secure_url").toString(),
                    result.get("public_id").toString());
        } catch (Exception e) {
            throw new IllegalStateException("Upload Cloudinary thất bại. Kiểm tra cấu hình kết nối.", e);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));
        } catch (Exception e) {
            throw new IllegalStateException("Xóa ảnh Cloudinary thất bại", e);
        }
    }
}
