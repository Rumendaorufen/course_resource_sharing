package com.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("course")
@EqualsAndHashCode(callSuper = true)
public class Course extends BaseEntity {

    private String name;
    
    private String description;
    
    private Long teacherId;
    
    @TableField(exist = false)
    private String teacherName;
}
