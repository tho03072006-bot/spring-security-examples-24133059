package vn.iotstar.config;

import java.util.Map;
import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
@Configuration @Profile("!demo & !test")
public class CloudinaryConfig {
    @Bean Cloudinary cloudinary(@Value("${cloudinary.cloud-name}") String name,@Value("${cloudinary.api-key}") String key,@Value("${cloudinary.api-secret}") String secret){
        return new Cloudinary(Map.of("cloud_name",name,"api_key",key,"api_secret",secret,"secure",true));
    }
}
