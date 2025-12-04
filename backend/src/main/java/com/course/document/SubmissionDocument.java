package com.course.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 作业提交文档实体类，用于MongoDB存储
 */
@Data
@Document(collection = "homework_submission")
public class SubmissionDocument {
    /**
     * MongoDB文档ID，使用UUID
     */
    @Id
    private String id;
    
    /**
     * 提交ID，与MySQL的homework_submission.id对应
     */
    private Long submissionId;
    
    /**
     * 作业ID
     */
    private Long assignmentId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 作业内容
     */
    private String content;
    
    /**
     * 附件信息
     */
    private Attachment attachment;
    
    /**
     * 提交状态
     */
    private String status;
    
    /**
     * 分数
     */
    private Integer score;
    
    /**
     * 评语
     */
    private String comment;
    
    /**
     * 反馈
     */
    private String feedback;
    
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    
    /**
     * 批改时间
     */
    private LocalDateTime gradeTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 附件内部类
     */
    @Data
    public static class Attachment {
        /**
         * 附件URL
         */
        private String url;
        
        /**
         * 附件名称
         */
        private String name;
        
        /**
         * 附件大小
         */
        private Long size;
    }
}