package com.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    @Select("SELECT c.* FROM course c " +
            "LEFT JOIN student_course sc ON c.id = sc.course_id " +
            "WHERE sc.student_id = #{studentId}")
    List<Course> findByStudentId(Long studentId);

    @Select("SELECT * FROM course WHERE teacher_id = #{teacherId}")
    List<Course> findByTeacherId(Long teacherId);

    List<Map<String, Object>> findAllWithDetails();

    Map<String, Object> findByIdWithDetails(Long id);

    @Select("SELECT COUNT(*) FROM course WHERE id = #{id}")
    int checkCourseExists(Long id);
}
