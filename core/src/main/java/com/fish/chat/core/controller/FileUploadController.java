package com.fish.chat.core.controller;

import com.fish.chat.common.properties.FileUploadProperties;
import com.fish.chat.common.result.Result;
import com.fish.chat.common.utils.FileUploadUtil;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Resource
    private FileUploadProperties fileUploadProperties;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = fileUploadProperties.getDir();
            String fileName = FileUploadUtil.upload(file, uploadDir);
            
            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("accessUrl", fileUploadProperties.getAccessPath() + fileName);
            
            return Result.success("上传成功", result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(fileUploadProperties.getDir())
                    .toAbsolutePath()
                    .normalize()
                    .resolve(fileName)
                    .normalize();
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
