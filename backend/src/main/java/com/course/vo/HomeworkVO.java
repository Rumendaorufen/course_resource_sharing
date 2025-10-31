package com.course.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkVO {
    private Long id;
    private String title;
    private String content;
    private Long courseId;
    private String courseName;
    private LocalDateTime deadline;
    private Integer submissionCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
