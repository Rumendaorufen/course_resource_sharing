package com.course.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业提交视图对象
 */
@Data
public class HomeworkSubmissionVO {
    /**
     * 提交记录ID
     */
    private Long id;

    /**
     * 作业ID
     */
    private Long assignmentId;

    /**
     * 作业标题
     */
    private String assignmentTitle;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 提交内容
     */
    private String content;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 附件大小（字节）
     */
    private Long attachmentSize;

    /**
     * 分数
     */
    private Integer score;

    /**
     * 教师评语
     */
    private String comment;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 批改时间
     */
    private LocalDateTime gradeTime;

    /**
     * 提交状态
     */
    private String status;

    /**
     * 是否逾期提交
     */
    private Boolean isLate;
}
