package com.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("homework")
@EqualsAndHashCode(callSuper = true)
public class Homework extends BaseEntity {

    private String title;
    
    private String description;
    
    private String content;
    
    private Long courseId;
    
    private Long teacherId;
    
    private LocalDateTime deadline;
    
    private Integer status;
}
