package com.course.service.impl;

import com.course.common.exception.ServiceException;
import com.course.dto.CourseDTO;
import com.course.entity.Course;
import com.course.entity.CourseSelection;
import com.course.entity.StudentCourse;
import com.course.entity.User;
import com.course.mapper.CourseMapper;
import com.course.mapper.CourseSelectionMapper;
import com.course.mapper.StudentCourseMapper;
import com.course.mapper.UserMapper;
import com.course.service.CourseService;
import com.course.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.course.common.constant.UserRoleConstants.ROLE_TEACHER;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final CourseSelectionMapper courseSelectionMapper;

    @Override
    @Transactional
    public void createCourse(CourseDTO courseDTO) {
        // 验证教师是否存在且是教师角色
        User teacher = userMapper.selectById(courseDTO.getTeacherId());
        if (teacher == null || !ROLE_TEACHER.equals(teacher.getRole())) {
            throw new ServiceException("教师不存在");
        }

        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setTeacherId(courseDTO.getTeacherId());

        courseMapper.insert(course);
    }

    @Override
    @Transactional
    public void updateCourse(Long id, CourseDTO courseDTO) {
        log.info("开始更新课程，ID: {}, 更新内容: {}", id, courseDTO);
        
        Course course = courseMapper.selectById(id);
        if (course == null) {
            log.warn("课程不存在，ID: {}", id);
            throw new ServiceException("课程不存在");
        }
        log.info("查询到原课程信息: {}", course);

        // 如果要更改教师，先验证新教师是否存在且是教师角色
        if (courseDTO.getTeacherId() != null && !courseDTO.getTeacherId().equals(course.getTeacherId())) {
            User teacher = userMapper.selectById(courseDTO.getTeacherId());
            if (teacher == null || !ROLE_TEACHER.equals(teacher.getRole())) {
                log.warn("教师不存在或不是教师角色，teacherId: {}", courseDTO.getTeacherId());
                throw new ServiceException("教师不存在");
            }
            log.info("验证新教师信息成功: {}", teacher);
        }

        // 更新课程信息
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setTeacherId(courseDTO.getTeacherId());

        int result = courseMapper.updateById(course);
        if (result <= 0) {
            log.error("更新课程失败，ID: {}", id);
            throw new ServiceException("更新课程失败");
        }
        log.info("课程更新成功，ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        log.info("开始删除课程，ID: {}", id);
        
        // 检查课程是否存在
        Course course = courseMapper.selectById(id);
        if (course == null) {
            log.warn("课程不存在，ID: {}", id);
            throw new ServiceException("课程不存在");
        }
        log.info("查询到课程信息: {}", course);

        // 先删除学生选课记录
        int deleteStudentCount = studentCourseMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentCourse>()
                .eq("course_id", id)
        );
        log.info("删除学生选课记录数: {}", deleteStudentCount);

        // 删除课程本身
        int result = courseMapper.deleteById(id);
        log.info("删除课程结果: {}", result);
        
        if (result <= 0) {
            log.error("删除课程失败，ID: {}", id);
            throw new ServiceException("删除课程失败");
        }
        
        // 验证课程是否真的被删除
        int exists = courseMapper.checkCourseExists(id);
        if (exists > 0) {
            log.error("课程仍然存在，删除可能失败，ID: {}", id);
            throw new ServiceException("删除课程失败，请重试");
        }
        
        log.info("课程删除成功，ID: {}", id);
    }

    @Override
    public CourseVO getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ServiceException("课程不存在");
        }
        
        CourseVO courseVO = new CourseVO();
        //BeanUtils.copyProperties(course, courseVO);
        
        // 获取教师信息
        User teacher = userMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            courseVO.setTeacherName(teacher.getRealName());
        }
        
        courseVO.setId(course.getId());
        courseVO.setName(course.getName());
        courseVO.setDescription(course.getDescription());
        courseVO.setTeacherId(course.getTeacherId());
        
        return courseVO;
    }

    @Override
    public List<Course> list() {
        List<Course> courses = courseMapper.selectList(null);
        
        // 获取所有教师ID
        List<Long> teacherIds = courses.stream()
                .map(Course::getTeacherId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询教师信息
        List<User> teachers = userMapper.selectBatchIds(teacherIds);
        Map<Long, String> teacherNames = teachers.stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
        
        // 设置教师姓名
        return courses.stream()
                .peek(course -> course.setTeacherName(teacherNames.get(course.getTeacherId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseVO> getAllCourses() {
        List<Map<String, Object>> coursesWithDetails = courseMapper.findAllWithDetails();
        return coursesWithDetails.stream().map(this::convertToCourseVO).collect(Collectors.toList());
    }

    private CourseVO convertToCourseVO(Map<String, Object> courseMap) {
        CourseVO courseVO = new CourseVO();
        courseVO.setId(((Number) courseMap.get("id")).longValue());
        courseVO.setName((String) courseMap.get("name"));
        courseVO.setDescription((String) courseMap.get("description"));
        courseVO.setTeacherId(((Number) courseMap.get("teacher_id")).longValue());
        courseVO.setTeacherName((String) courseMap.get("teacher_real_name"));
        courseVO.setStudentCount(((Number) courseMap.get("studentCount")).intValue());
        courseVO.setResourceCount(((Number) courseMap.get("resourceCount")).intValue());
        courseVO.setHomeworkCount(((Number) courseMap.get("homeworkCount")).intValue());
        return courseVO;
    }

    @Override
    public List<Course> getTeacherCourses(Long teacherId) {
        return courseMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<Course> getStudentCourses(Long studentId) {
        return courseMapper.findByStudentId(studentId);
    }

    @Override
    @Transactional
    public void selectCourse(Long studentId, Long courseId) {
        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new ServiceException("课程不存在");
        }

        // 检查是否已经选过这门课
        if (studentCourseMapper.isEnrolled(studentId, courseId)) {
            throw new ServiceException("已经选过这门课程");
        }

        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(studentId);
        studentCourse.setCourseId(courseId);

        studentCourseMapper.insert(studentCourse);
    }

    @Override
    @Transactional
    public void dropCourse(Long studentId, Long courseId) {
        // 检查是否选过这门课
        if (!studentCourseMapper.isEnrolled(studentId, courseId)) {
            throw new ServiceException("未选择这门课程");
        }

        studentCourseMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentCourse>()
                .eq("student_id", studentId)
                .eq("course_id", courseId)
        );
    }

    @Override
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        return studentCourseMapper.isEnrolled(studentId, courseId);
    }
}
