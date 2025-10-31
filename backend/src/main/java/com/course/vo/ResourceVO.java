package com.course.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResourceVO {
    private Long id;
    private String name;
    private String description;
    private Long courseId;
    private String courseName;
    private Long uploaderId;
    private String uploaderName;
    private String type;
    private String originalFileName;
    private Long fileSize;
    private Integer downloadCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
