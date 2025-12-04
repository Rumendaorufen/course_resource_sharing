package com.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MongoDB清理记录实体类
 */
@Data
@TableName("mongo_cleanup")
public class MongoCleanup {
    /**
     * 清理ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * MongoDB文档ID
     */
    private String mongoId;
    
    /**
     * 集合名称
     */
    private String collection;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 状态：0-待清理，1-已清理
     */
    private Integer status;
    
    /**
     * 清理时间
     */
    private LocalDateTime cleanupTime;
}