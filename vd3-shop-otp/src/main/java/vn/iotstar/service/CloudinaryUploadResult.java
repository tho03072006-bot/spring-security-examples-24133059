package vn.iotstar.service;

// URL để hiển thị ảnh, publicId để xóa ảnh sau này
public record CloudinaryUploadResult(String url, String publicId) {
}
