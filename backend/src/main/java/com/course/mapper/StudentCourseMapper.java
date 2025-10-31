package com.course.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.course.entity.StudentCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学生选课记录Mapper接口
 */
@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {
    
    /**
     * 检查记录是否存在
     * @param queryWrapper 查询条件
     * @return 是否存在
     */
    @Select("SELECT EXISTS(${ew.customSqlSegment})")
    boolean exists(@Param("ew") Wrapper<StudentCourse> queryWrapper);
    
    /**
     * 检查学生是否已选修课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否已选修
     */
    @Select("SELECT COUNT(*) > 0 FROM student_course WHERE student_id = #{studentId} AND course_id = #{courseId}")
    boolean isEnrolled(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
