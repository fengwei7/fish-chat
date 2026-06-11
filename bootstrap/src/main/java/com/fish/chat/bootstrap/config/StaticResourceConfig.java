package com.fish.chat.bootstrap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.nio.file.Paths;

/**
 * 静态资源配置
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    /**
     * 文件上传保存目录（相对路径或绝对路径）
     */
    @Value("${fish-chat-config.file.upload.dir}")
    private String dir = "./uploads";

    /**
     * 上传文件的访问 URL 前缀
     */
    @Value("${fish-chat-config.file.upload.access-path}")
    private String accessPath = "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源路径
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 上传文件目录映射为静态资源
        String accessPath = this.accessPath;
        if (!accessPath.startsWith("/")) {
            accessPath = "/" + accessPath;
        }
        if (!accessPath.endsWith("/")) {
            accessPath = accessPath + "/";
        }

        // 将相对路径转换为绝对路径
        String uploadDir = Paths.get(this.dir)
                .toAbsolutePath()
                .normalize()
                .toString();
        
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