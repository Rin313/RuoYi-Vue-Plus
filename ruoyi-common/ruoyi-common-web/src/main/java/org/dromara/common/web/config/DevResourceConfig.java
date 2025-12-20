package org.dromara.common.web.config;

import java.io.File;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("dev")  // 只在开发环境生效
public class DevResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /upload/** 映射到文件存储目录
        String location = "file:" + Paths.get("./upload").toAbsolutePath().normalize() + File.separator;
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }
}