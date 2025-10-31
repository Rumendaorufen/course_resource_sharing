package com.course.vo;

import lombok.Data;

@Data
public class CourseVO {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Integer studentCount;
    private Integer resourceCount;
    private Integer homeworkCount;
}
