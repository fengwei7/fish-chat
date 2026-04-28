package com.fish.chat.common.utils;

import com.fish.chat.common.enums.ErrorCodeEnum;
import com.fish.chat.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Component
public class FileUploadUtil {

    // 默认允许的文件类型（可根据需求修改）
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xlsx", "txt"};

    // 默认最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 上传文件到指定目录 调用该方法
     *
     * @param file       上传的 MultipartFile
     * @param uploadDir  保存目录（相对路径或绝对路径）
     * @return           保存后的文件名（含扩展名）
     * @throws IOException 抛出的异常
     */
    public static String upload(MultipartFile file, String uploadDir) throws IOException {
        return upload(file, uploadDir, ALLOWED_EXTENSIONS, MAX_FILE_SIZE);
    }

    /**
     * 上传文件（自定义允许的扩展名和最大大小）
     *
     * @param file             上传的文件
     * @param uploadDir        保存目录
     * @param allowedExtensions 允许的扩展名数组（如 {"png", "jpg"}）
     * @param maxSize          最大文件大小（字节）
     * @return                 保存后的文件名
     * @throws IOException     抛出的异常
     */
    public static String upload(MultipartFile file, String uploadDir, String[] allowedExtensions, long maxSize)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw ErrorCodeEnum.FILE_EMPTY.toException();
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCodeEnum.FILE_TOO_LARGE.getCode(), ErrorCodeEnum.FILE_TOO_LARGE.getMessage() + ":" + maxSize / (1024 * 1024) + "MB");
        }

        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw ErrorCodeEnum.INVALID_FILENAME.toException();
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!Arrays.asList(allowedExtensions).contains(extension)) {
            throw new BusinessException(ErrorCodeEnum.INVALID_FILETYPE.getCode(), ErrorCodeEnum.INVALID_FILETYPE.getMessage() + ":" + extension);
        }

        // 创建唯一文件名
        String uniqueFileName = UUID.randomUUID() + "." + extension;

        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFileName);
        file.transferTo(filePath.toFile());

        return uniqueFileName;
    }

    /**
     * 获取文件扩展名（不带点）
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw ErrorCodeEnum.INVALID_FILE_EXTENSION.toException();
        }
        return fileName.substring(lastDotIndex + 1);
    }
}