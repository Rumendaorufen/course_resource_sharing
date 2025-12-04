package com.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.course.entity.MongoCleanup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MongoDB清理记录Mapper接口
 */
@Mapper
public interface MongoCleanupMapper extends BaseMapper<MongoCleanup> {
    /**
     * 根据状态查询清理记录
     * @param status 状态：0-待清理，1-已清理
     * @return 清理记录列表
     */
    List<MongoCleanup> selectByStatus(Integer status);
}