package com.course.service;

import com.course.dto.CourseDTO;
import com.course.entity.Course;
import com.course.vo.CourseVO;

import java.util.List;

public interface CourseService {
    /**
     * 创建课程
     */
    void createCourse(CourseDTO courseDTO);

    /**
     * 更新课程信息
     */
    void updateCourse(Long id, CourseDTO courseDTO);

    /**
     * 删除课程
     */
    void deleteCourse(Long id);

    /**
     * 获取课程详情
     */
    CourseVO getCourseById(Long id);

    /**
     * 获取教师的所有课程
     */
    List<Course> getTeacherCourses(Long teacherId);

    /**
     * 获取学生的所有课程
     */
    List<Course> getStudentCourses(Long studentId);

    /**
     * 学生选课
     */
    void selectCourse(Long studentId, Long courseId);

    /**
     * 学生退课
     */
    void dropCourse(Long studentId, Long courseId);

    /**
     * 获取所有课程
     */
    List<CourseVO> getAllCourses();

    /**
     * 获取所有课程列表
     */
    List<Course> list();

    /**
     * 检查学生是否已选修课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否已选修
     */
    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);
}
