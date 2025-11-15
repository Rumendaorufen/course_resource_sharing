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

import java.text.SimpleDateFormat;
import java.util.*;
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
    
    @Override
    public Map<String, List<?>> getMonthlyStats() {
        Map<String, List<?>> result = new HashMap<>();
        
        // 获取最近6个月的月份标签
        List<String> months = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        
        for (int i = 5; i >= 0; i--) {
            Calendar tempCal = (Calendar) cal.clone();
            tempCal.add(Calendar.MONTH, -i);
            months.add(sdf.format(tempCal.getTime()));
        }
        
        result.put("months", months);
        
        // 获取每个月的资源数量
        List<Integer> resourceCounts = new ArrayList<>();
        // 获取每个月的作业数量
        List<Integer> assignmentCounts = new ArrayList<>();
        
        for (String month : months) {
            String startDate = month + "-01";
            String endDate = month + "-31";
            
            // 统计当月资源数量
            LambdaQueryWrapper<Resource> resourceWrapper = new LambdaQueryWrapper<Resource>()
                .apply("DATE_FORMAT(create_time, '%Y-%m-%d') >= {0}", startDate)
                .apply("DATE_FORMAT(create_time, '%Y-%m-%d') <= {0}", endDate);
            Long resourceCount = resourceMapper.selectCount(resourceWrapper);
            resourceCounts.add(resourceCount.intValue());
            
            // 统计当月作业数量
            LambdaQueryWrapper<Assignment> assignmentWrapper = new LambdaQueryWrapper<Assignment>()
                .apply("DATE_FORMAT(create_time, '%Y-%m-%d') >= {0}", startDate)
                .apply("DATE_FORMAT(create_time, '%Y-%m-%d') <= {0}", endDate);
            Long assignmentCount = assignmentMapper.selectCount(assignmentWrapper);
            assignmentCounts.add(assignmentCount.intValue());
        }
        
        result.put("resources", resourceCounts);
        result.put("assignments", assignmentCounts);
        
        return result;
    }
}
