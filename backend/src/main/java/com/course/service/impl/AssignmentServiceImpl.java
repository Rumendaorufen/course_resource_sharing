package com.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.course.common.exception.ServiceException;
import com.course.dto.AssignmentDTO;
import com.course.entity.Assignment;
import com.course.entity.Course;
import com.course.entity.StudentCourse;
import com.course.entity.User;
import com.course.mapper.AssignmentMapper;
import com.course.mapper.CourseMapper;
import com.course.mapper.StudentCourseMapper;
import com.course.mapper.UserMapper;
import com.course.service.AssignmentService;
import com.course.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAssignment(AssignmentDTO assignmentDTO) {
        log.info("开始创建作业");
        
        // 获取当前用户ID并验证身份
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.debug("当前用户ID: {}", currentUserId);

        // 验证课程是否存在
        Course course = courseMapper.selectById(assignmentDTO.getCourseId());
        if (course == null) {
            throw new ServiceException("课程不存在");
        }

        // 验证当前用户是否为教师
        if (!course.getTeacherId().equals(currentUserId)) {
            throw new ServiceException("只能在自己的课程中发布作业");
        }

        try {
            // 创建作业
            Assignment assignment = new Assignment();
            BeanUtils.copyProperties(assignmentDTO, assignment);
            assignment.setTeacherId(currentUserId);
            assignment.setCreateTime(LocalDateTime.now());
            assignment.setStatus("active");
            assignmentMapper.insert(assignment);
            log.info("作业创建成功, ID: {}", assignment.getId());
        } catch (Exception e) {
            log.error("作业创建失败", e);
            throw new ServiceException("作业创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssignment(Long id, AssignmentDTO assignmentDTO) {
        log.info("开始更新作业, ID: {}", id);
        try {
            // 验证作业是否存在
            Assignment assignment = assignmentMapper.selectById(id);
            if (assignment == null) {
                throw new ServiceException("作业不存在");
            }

            // 验证课程是否存在
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course == null) {
                throw new ServiceException("课程不存在");
            }

            // 验证是否为课程教师
            if (!course.getTeacherId().equals(SecurityUtils.getCurrentUserId())) {
                throw new ServiceException("无权修改此作业");
            }

            // 更新作业
            BeanUtils.copyProperties(assignmentDTO, assignment);
            assignment.setUpdateTime(LocalDateTime.now());
            assignmentMapper.updateById(assignment);
            log.info("作业更新成功");
        } catch (Exception e) {
            log.error("作业更新失败", e);
            throw new ServiceException("作业更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssignment(Long id) {
        log.info("开始删除作业, ID: {}", id);
        try {
            // 验证作业是否存在
            Assignment assignment = assignmentMapper.selectById(id);
            if (assignment == null) {
                throw new ServiceException("作业不存在");
            }

            // 验证课程是否存在
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course == null) {
                throw new ServiceException("课程不存在");
            }

            // 验证是否为课程教师
            if (!course.getTeacherId().equals(SecurityUtils.getCurrentUserId())) {
                throw new ServiceException("无权删除此作业");
            }

            // 逻辑删除
            assignment.setStatus("inactive");
            assignment.setUpdateTime(LocalDateTime.now());
            assignmentMapper.updateById(assignment);
            log.info("作业删除成功");
        } catch (Exception e) {
            log.error("作业删除失败", e);
            throw new ServiceException("作业删除失败: " + e.getMessage());
        }
    }

    @Override
    public Assignment getAssignmentById(Long id) {
        log.info("获取作业详情, ID: {}", id);
        try {
            Assignment assignment = assignmentMapper.selectById(id);
            if (assignment == null) {
                throw new ServiceException("作业不存在");
            }

            // 获取课程信息
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                assignment.setCourseName(course.getName());
            }

            // 获取教师信息
            User teacher = userMapper.selectById(assignment.getTeacherId());
            if (teacher != null) {
                assignment.setTeacherName(teacher.getRealName());
            }

            return assignment;
        } catch (Exception e) {
            log.error("获取作业详情失败", e);
            throw new ServiceException("获取作业详情失败: " + e.getMessage());
        }
    }

    @Override
    public List<Assignment> getAssignmentsByTeacherId(Long teacherId) {
        return assignmentMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<Assignment> getAssignmentsByStudentId(Long studentId) {
        // 获取学生选修的所有课程ID
        QueryWrapper<StudentCourse> courseWrapper = new QueryWrapper<>();
        courseWrapper.select("id", "student_id", "course_id", "create_time")
                    .eq("student_id", studentId);
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(courseWrapper);
        
        // 获取这些课程的所有作业
        List<Long> courseIds = studentCourses.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());
                
        if (courseIds.isEmpty()) {
            return Collections.emptyList(); // 返回空列表
        }
        
        QueryWrapper<Assignment> assignmentWrapper = new QueryWrapper<>();
        assignmentWrapper.in("course_id", courseIds)
                        .eq("status", "active")  // 只获取活动状态的作业
                        .orderByDesc("create_time");
        List<Assignment> assignments = assignmentMapper.selectList(assignmentWrapper);
        
        // 填充课程和教师信息
        for (Assignment assignment : assignments) {
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                assignment.setCourseName(course.getName());
            }
            
            User teacher = userMapper.selectById(assignment.getTeacherId());
            if (teacher != null) {
                assignment.setTeacherName(teacher.getRealName());
            }
        }
        
        return assignments;
    }

    @Override
    public List<Assignment> getAssignmentsByCourseId(Long courseId) {
        QueryWrapper<Assignment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId)
                   .orderByDesc("create_time");
        return assignmentMapper.selectList(queryWrapper);
    }

    @Override
    public boolean isAssignmentDeadlinePassed(Long assignmentId) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new ServiceException("作业不存在");
        }
        return LocalDateTime.now().isAfter(assignment.getDeadline());
    }

    @Override
    public List<Assignment> getActiveAssignments() {
        return baseMapper.selectList(new QueryWrapper<Assignment>()
                .eq("status", "active")
                .orderByDesc("create_time"));
    }
}
