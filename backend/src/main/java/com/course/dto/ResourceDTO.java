package com.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ResourceDTO {
    @NotBlank(message = "资源名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private String type;
}
