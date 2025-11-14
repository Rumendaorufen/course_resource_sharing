package com.course.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 作业提交数据传输对象
 */
@Data
public class HomeworkSubmissionDTO {
    /**
     * 作业ID
     */
    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    
    /**
     * 作业内容
     */
    private String content;
    
    /**
     * 附件URL
     */
    private String attachmentUrl;
    
    /**
     * 附件名称
     */
    private String attachmentName;
    
    /**
     * 附件大小（字节）
     */
    private Long attachmentSize;
    
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
}
