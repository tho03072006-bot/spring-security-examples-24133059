package vn.iotstar.config;

import java.nio.file.Path;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
@Configuration @Profile({"demo","test"})
public class DemoWebConfig implements WebMvcConfigurer {
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/uploads/**").addResourceLocations(Path.of("uploads").toAbsolutePath().toUri().toString()+"/");
    }
}
