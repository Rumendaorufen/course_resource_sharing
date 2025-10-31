package com.course.service;

import com.course.exception.FileStorageException;
import com.course.utils.FileValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file-upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("无法创建文件上传目录", ex);
        }
    }

    /**
     * 保存作业提交文件
     * @param file 文件
     * @param studentId 学生ID
     * @param assignmentId 作业ID
     * @return 文件URL
     */
    public String storeSubmissionFile(MultipartFile file, Long studentId, Long assignmentId) {
        // 验证文件
        String validationError = FileValidationUtils.validateFile(file);
        if (validationError != null) {
            throw new FileStorageException(validationError);
        }

        // 生成文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String safeFilename = FileValidationUtils.getSafeFilename(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("%d_%d_%s_%s_%s", 
            studentId, assignmentId, timestamp, uniqueId, safeFilename);

        try {
            // 检查文件名是否包含非法字符
            if (filename.contains("..")) {
                throw new FileStorageException("文件名包含非法字符");
            }

            // 创建学生作业目录
            Path studentDir = this.fileStorageLocation.resolve(
                Paths.get(String.valueOf(studentId), String.valueOf(assignmentId)));
            Files.createDirectories(studentDir);

            // 保存文件
            Path targetLocation = studentDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径
            return String.format("/uploads/%d/%d/%s", studentId, assignmentId, filename);
        } catch (IOException ex) {
            throw new FileStorageException("无法保存文件 " + filename, ex);
        }
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                String relativePath = fileUrl.substring("/uploads/".length());
                Path filePath = this.fileStorageLocation.resolve(relativePath);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            log.error("删除文件失败: {}", fileUrl, ex);
        }
    }
}
