package vn.iotstar.config;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Demo/test lưu ảnh ở thư mục uploads/ thay cho Cloudinary; phục vụ qua URL /uploads/**
@Configuration
@Profile({"demo", "test"})
public class DemoWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of("uploads").toAbsolutePath().toUri() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
