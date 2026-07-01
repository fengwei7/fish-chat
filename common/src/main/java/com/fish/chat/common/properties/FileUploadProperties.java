package com.fish.chat.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件上传配置属性
 */
@Data
@ConfigurationProperties(prefix = "fish-chat-config.file.upload")
public class FileUploadProperties {

    /**
     * 文件上传保存目录（相对路径或绝对路径）
     */
    private String dir = "./uploads";

    /**
     * 上传文件的访问 URL 前缀
     */
    private String accessPath = "/uploads/";
}
