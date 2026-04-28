package com.fish.chat.bootstrap.config;

import com.fish.chat.common.properties.FileUploadProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Autowired
    private FileUploadProperties fileUploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源路径
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 上传文件目录映射为静态资源
        String accessPath = fileUploadProperties.getAccessPath();
        if (!accessPath.startsWith("/")) {
            accessPath = "/" + accessPath;
        }
        if (!accessPath.endsWith("/")) {
            accessPath = accessPath + "/";
        }

        String uploadDir = fileUploadProperties.getDir();
        if (!uploadDir.endsWith("/") && !uploadDir.endsWith("\\")) {
            uploadDir = uploadDir + "/";
        }
        if (!uploadDir.startsWith("file:")) {
            uploadDir = "file:" + uploadDir;
        }

        registry.addResourceHandler(accessPath + "**")
                .addResourceLocations(uploadDir);
    }
}