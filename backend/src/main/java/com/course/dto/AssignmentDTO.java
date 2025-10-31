package com.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AssignmentDTO {
    @NotBlank(message = "作业标题不能为空")
    private String title;

    @NotBlank(message = "作业描述不能为空")
    private String description;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;
    
    private String status = "active";
}
