package com.course.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileService {
    /**
     * 存储文件
     * @param file 文件
     * @return 文件存储路径
     */
    String storeFile(MultipartFile file) throws IOException;

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);

    /**
     * 获取文件下载URL
     * @param filePath 文件路径
     * @return 下载URL
     */
    String getFileUrl(String filePath);

    /**
     * 检查文件类型是否允许
     * @param fileName 文件名
     * @return 是否允许
     */
    boolean isAllowedFileType(String fileName);
}
