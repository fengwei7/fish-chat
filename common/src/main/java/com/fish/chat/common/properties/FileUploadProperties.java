package com.fish.chat.common.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 */
@Data
@Component
public class FileUploadProperties {

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
}
