package com.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assignment")  // 指定表名为 assignment
public class Assignment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    @TableField("teacher_id")  // 明确指定字段映射
    private Long teacherId;
    private LocalDateTime deadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String status;
    
    @TableField(exist = false)
    private String courseName;
    
    @TableField(exist = false)
    private String teacherName;
}
