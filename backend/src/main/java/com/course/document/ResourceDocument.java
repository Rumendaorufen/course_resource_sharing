package com.course.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 资源文档实体类，用于MongoDB存储
 */
@Data
@Document(collection = "resource")
public class ResourceDocument {
    /**
     * MongoDB文档ID，使用UUID
     */
    @Id
    private String id;
    
    /**
     * 资源ID，与MySQL的resource.id对应
     */
    private Long resourceId;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源描述
     */
    private String description;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 上传者ID
     */
    private Long uploaderUserId;
    
    /**
     * 上传者名称
     */
    private String uploaderName;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 资源类型
     */
    private String type;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}