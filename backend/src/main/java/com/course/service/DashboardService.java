package com.course.service;

import com.course.vo.AssignmentVO;
import com.course.vo.ResourceVO;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    /**
     * 获取统计数据
     * @return 包含课程数、资源数、作业数的统计信息
     */
    Map<String, Integer> getStats();

    /**
     * 获取最近的作业列表
     * @return 最近的作业列表
     */
    List<AssignmentVO> getRecentAssignments();

    /**
     * 获取最新的资源列表
     * @return 最新的资源列表
     */
    List<ResourceVO> getRecentResources();
    
    /**
     * 获取月度统计数据
     * @return 包含月份、资源数量、作业数量的统计信息
     */
    Map<String, List<?>> getMonthlyStats();
}
