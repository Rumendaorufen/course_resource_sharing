package com.course.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentVO {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime deadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String status;
    
    // 提交相关字段
    private String submissionStatus;
    private Long submissionId;
    private LocalDateTime submissionTime;
    private Integer score;
}
