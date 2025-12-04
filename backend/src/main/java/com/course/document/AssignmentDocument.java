package com.course.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 作业文档实体类，用于MongoDB存储
 */
@Data
@Document(collection = "assignment")
public class AssignmentDocument {
    /**
     * MongoDB文档ID，使用UUID
     */
    @Id
    private String id;
    
    /**
     * 作业ID，与MySQL的assignment.id对应
     */
    private Long assignmentId;
    
    /**
     * 作业标题
     */
    private String title;
    
    /**
     * 作业描述
     */
    private String description;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师名称
     */
    private String teacherName;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}