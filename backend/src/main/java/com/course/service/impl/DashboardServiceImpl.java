package com.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.entity.Assignment;
import com.course.entity.Course;
import com.course.entity.Resource;
import com.course.mapper.AssignmentMapper;
import com.course.mapper.CourseMapper;
import com.course.mapper.ResourceMapper;
import com.course.service.DashboardService;
import com.course.vo.AssignmentVO;
import com.course.vo.ResourceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CourseMapper courseMapper;
    private final ResourceMapper resourceMapper;
    private final AssignmentMapper assignmentMapper;

    @Override
    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        
        // 获取课程总数
        Long courseCount = courseMapper.selectCount(new LambdaQueryWrapper<Course>());
        stats.put("courses", courseCount.intValue());
        
        // 获取资源总数
        Long resourceCount = resourceMapper.selectCount(new LambdaQueryWrapper<Resource>());
        stats.put("resources", resourceCount.intValue());
        
        // 获取作业总数
        Long assignmentCount = assignmentMapper.selectCount(new LambdaQueryWrapper<Assignment>());
        stats.put("assignments", assignmentCount.intValue());
        
        return stats;
    }

    @Override
    public List<AssignmentVO> getRecentAssignments() {
        // 获取最近10个作业
        LambdaQueryWrapper<Assignment> queryWrapper = new LambdaQueryWrapper<Assignment>()
            .orderByDesc(Assignment::getCreateTime)
            .last("LIMIT 10");
            
        return assignmentMapper.selectList(queryWrapper)
            .stream()
            .map(assignment -> {
                AssignmentVO vo = new AssignmentVO();
                vo.setId(assignment.getId());
                vo.setTitle(assignment.getTitle());
                vo.setDeadline(assignment.getDeadline());
                // 获取课程名称
                Course course = courseMapper.selectById(assignment.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getName());
                }
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<ResourceVO> getRecentResources() {
        // 获取最近10个资源
        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<Resource>()
            .orderByDesc(Resource::getCreateTime)
            .last("LIMIT 10");
            
        return resourceMapper.selectList(queryWrapper)
            .stream()
            .map(resource -> {
                ResourceVO vo = new ResourceVO();
                vo.setId(resource.getId());
                vo.setName(resource.getName());
                vo.setCreateTime(resource.getCreateTime());
                // 获取课程名称
                Course course = courseMapper.selectById(resource.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getName());
                }
                return vo;
            })
            .collect(Collectors.toList());
    }
}
