package vn.iotstar.service.impl;

import org.springframework.web.multipart.MultipartFile;
public final class ImageValidation {
    private ImageValidation(){}
    public static byte[] bytes(MultipartFile file){
        if(file==null || file.isEmpty())throw new IllegalArgumentException("Chưa chọn ảnh");
        if(file.getSize()>10*1024*1024)throw new IllegalArgumentException("Ảnh tối đa 10 MB");
        try{
            byte[] bytes=file.getBytes();
            if(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes))==null)throw new IllegalArgumentException("Chỉ chấp nhận ảnh PNG, JPEG, GIF hoặc BMP hợp lệ");
            return bytes;
        }catch(java.io.IOException e){throw new IllegalArgumentException("Không đọc được ảnh",e);}
    }
}
