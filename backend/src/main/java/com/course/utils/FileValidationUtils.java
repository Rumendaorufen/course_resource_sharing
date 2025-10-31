package com.course.utils;

import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件验证工具类
 */
public class FileValidationUtils {
    
    // 文件大小限制
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    // 允许的文件类型
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "pdf", "doc", "docx", "txt", "zip", "rar", 
        "jpg", "jpeg", "png", "gif"
    ));

    /**
     * 验证文件大小
     * @param size 文件大小（字节）
     * @return 是否符合大小限制
     */
    public static boolean validateFileSize(long size) {
        return size <= MAX_FILE_SIZE;
    }

    /**
     * 验证文件类型
     * @param filename 文件名
     * @return 是否是允许的文件类型
     */
    public static boolean validateFileType(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 验证MultipartFile
     * @param file 上传的文件
     * @return 错误信息，如果没有错误则返回null
     */
    public static String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "文件不能为空";
        }
        
        if (!validateFileSize(file.getSize())) {
            return "文件大小不能超过10MB";
        }
        
        if (!validateFileType(file.getOriginalFilename())) {
            return "不支持的文件类型";
        }
        
        return null;
    }

    /**
     * 获取安全的文件名
     * @param originalFilename 原始文件名
     * @return 安全的文件名
     */
    public static String getSafeFilename(String originalFilename) {
        return originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
