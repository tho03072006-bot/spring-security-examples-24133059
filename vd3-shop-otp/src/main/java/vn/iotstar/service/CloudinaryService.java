package vn.iotstar.service;

import org.springframework.web.multipart.MultipartFile;

// Lưu trữ ảnh sản phẩm: Cloudinary thật, hoặc thư mục local ở profile demo/test
public interface CloudinaryService {

    CloudinaryUploadResult upload(MultipartFile file);

    void delete(String publicId);
}
