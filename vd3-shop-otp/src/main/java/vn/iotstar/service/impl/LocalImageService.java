package vn.iotstar.service.impl;

import java.nio.file.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.*;
@Service @Profile({"demo","test"})
public class LocalImageService implements CloudinaryService {
    public CloudinaryUploadResult upload(MultipartFile file){
        byte[] bytes=ImageValidation.bytes(file);
        String id=UUID.randomUUID()+".png";
        try{Files.createDirectories(Path.of("uploads"));Files.write(Path.of("uploads",id),bytes);}
        catch(java.io.IOException e){throw new IllegalStateException("Không lưu được ảnh demo",e);}
        return new CloudinaryUploadResult("/uploads/"+id,id);
    }
    public void delete(String id){
        if(id==null || id.isBlank())return;
        Path root=Path.of("uploads").toAbsolutePath().normalize(),file=root.resolve(id).normalize();
        if(!file.startsWith(root))throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ");
        try{Files.deleteIfExists(file);}catch(java.io.IOException e){throw new IllegalStateException("Không xóa được ảnh demo",e);}
    }
}
