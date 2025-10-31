package com.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CourseDTO {
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;
}
