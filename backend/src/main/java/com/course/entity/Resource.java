package com.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("resource")
@EqualsAndHashCode(callSuper = true)
public class Resource extends BaseEntity {

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
    @TableField("file_path")
    private String filePath;
    
    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 文件大小
     */
    @TableField("file_size")
    private Long fileSize;
    
    /**
     * 上传者ID
     */
    @TableField("uploader_user_id")
    private Long uploaderUserId;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;
    
    /**
     * 课程名称
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * 上传者名称
     */
    @TableField(exist = false)
    private String uploaderName;
    
    /**
     * 下载次数
     */
    @TableField("download_count")
    private Integer downloadCount;
    
    /**
     * 资源类型
     */
    private String type;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public Long getUploaderUserId() {
        return uploaderUserId;
    }

    public void setUploaderUserId(Long uploaderUserId) {
        this.uploaderUserId = uploaderUserId;
    }
}
