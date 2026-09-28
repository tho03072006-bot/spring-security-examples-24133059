package vn.iotstar.service.impl;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.*;
@Service @Profile("!demo & !test") @RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
    public CloudinaryUploadResult upload(MultipartFile file){
        byte[] bytes=ImageValidation.bytes(file);
        try{
            Map<?,?> result=cloudinary.uploader().upload(bytes,Map.of("folder","security-assignment/products","resource_type","image"));
            return new CloudinaryUploadResult(result.get("secure_url").toString(),result.get("public_id").toString());
        }catch(Exception e){throw new IllegalStateException("Upload Cloudinary thất bại. Kiểm tra cấu hình kết nối.",e);}
    }
    public void delete(String id){
        if(id==null || id.isBlank())return;
        try{cloudinary.uploader().destroy(id,Map.of("resource_type","image"));}
        catch(Exception e){throw new IllegalStateException("Xóa ảnh Cloudinary thất bại",e);}
    }
}
