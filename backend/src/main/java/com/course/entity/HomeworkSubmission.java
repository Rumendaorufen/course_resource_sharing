package com.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交实体类
 */
@Data
@TableName("homework_submission")
public class HomeworkSubmission {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
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
     * 附件URL
     */
    private String attachmentUrl;
    
    /**
     * 附件大小
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
}
