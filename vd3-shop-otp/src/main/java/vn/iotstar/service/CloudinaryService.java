package vn.iotstar.service;

public interface CloudinaryService {
CloudinaryUploadResult upload(org.springframework.web.multipart.MultipartFile file); void delete(String publicId);
}
