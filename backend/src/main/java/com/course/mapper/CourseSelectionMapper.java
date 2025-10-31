package com.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.course.entity.CourseSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 选课记录Mapper接口
 */
@Mapper
public interface CourseSelectionMapper extends BaseMapper<CourseSelection> {
    
    /**
     * 检查学生是否已选修课程
     */
    @Select("SELECT COUNT(*) > 0 FROM course_selection WHERE student_id = #{studentId} AND course_id = #{courseId} AND status = 1")
    boolean exists(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
