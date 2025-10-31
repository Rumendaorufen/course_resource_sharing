package com.course.service.impl;

import com.course.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.InitializingBean;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService, InitializingBean {

    @Value("${file.upload.base-path}")
    private String baseUploadPath;

    @Value("${file.download.base-url}")
    private String baseDownloadUrl;
    
    @Value("${file.upload.allowed-types}")
    private String allowedTypes;
    
    private Set<String> allowedExtensions;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        // 从配置中初始化允许的文件扩展名集合
        allowedExtensions = new HashSet<>();
        if (StringUtils.hasText(allowedTypes)) {
            String[] types = allowedTypes.split(",");
            for (String type : types) {
                // 移除点号并转为小写
                String extension = type.trim().replace(".", "").toLowerCase();
                if (StringUtils.hasText(extension)) {
                    allowedExtensions.add(extension);
                }
            }
        }
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (!isAllowedFileType(originalFilename)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        // 先将baseUploadPath转换为绝对路径，避免相对路径导致的问题
        Path basePath = Paths.get(baseUploadPath).toAbsolutePath().normalize();
        
        // 使用日期作为子目录
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDirPath = basePath.resolve(dateStr);
        
        // 创建目录
        Files.createDirectories(uploadDirPath);
        log.debug("创建上传目录: {}", uploadDirPath);

        // 生成唯一的文件名
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
        
        // 构建目标路径
        Path targetPath = uploadDirPath.resolve(newFilename);
        log.debug("目标文件路径: {}", targetPath);
        
        // 使用Files.copy方法替代transferTo，避免临时文件路径问题
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("文件保存成功: {}", targetPath);
        
        return targetPath.toString();
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            log.debug("删除文件: {}", path);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return null;
        }
        // 转换为URL路径
        Path path = Paths.get(filePath);
        String relativePath = Paths.get(baseUploadPath).relativize(path).toString().replace('\\', '/');
        return baseDownloadUrl + "/" + relativePath;
    }

    @Override
    public boolean isAllowedFileType(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return false;
        }
        // 如果allowedExtensions为空，则允许所有文件类型
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return true;
        }
        String extension = StringUtils.getFilenameExtension(filename);
        return extension != null && allowedExtensions.contains(extension.toLowerCase());
    }
}
