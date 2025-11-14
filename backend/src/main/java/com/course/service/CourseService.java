package com.course.service;

import com.course.dto.CourseDTO;
import com.course.entity.Course;
import com.course.entity.User;
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
    
    /**
     * 按班级批量添加学生到课程
     * @param courseId 课程ID
     * @param classname 班级名称
     */
    void addStudentsByClass(Long courseId, String classname);
    
    /**
     * 添加单个学生到课程
     * @param courseId 课程ID
     * @param studentId 学生ID
     */
    void addStudentToCourse(Long courseId, Long studentId);
    
    /**
     * 从课程中移除学生
     * @param courseId 课程ID
     * @param studentId 学生ID
     */
    void removeStudentFromCourse(Long courseId, Long studentId);
    
    /**
     * 获取课程中的学生列表
     * @param courseId 课程ID
     * @return 学生列表
     */
    List<User> getStudentsInCourse(Long courseId);
    
    /**
     * 获取不在课程中的学生列表
     * @param courseId 课程ID
     * @param keyword 搜索关键词
     * @param classname 班级名称
     * @return 学生列表
     */
    List<User> getStudentsNotInCourse(Long courseId, String keyword, String classname);
    
    /**
     * 获取所有班级名称列表
     * @return 班级名称列表
     */
    List<String> getAllClassNames();
}
