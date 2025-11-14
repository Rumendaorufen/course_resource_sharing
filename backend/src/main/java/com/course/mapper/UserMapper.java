package com.course.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.course.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    User findByUsername(String username);

    List<User> findByRole(@Param("role") String role);
    
    /**
     * 根据班级名称查询学生
     */
    List<User> findByClassname(@Param("classname") String classname);
    
    /**
     * 查询课程中的学生列表
     */
    List<User> findStudentsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 查询不在课程中的学生列表
     */
    List<User> findStudentsNotInCourse(@Param("courseId") Long courseId, @Param("ew") QueryWrapper<User> queryWrapper);
    
    /**
     * 获取所有班级名称
     */
    List<String> findAllClassNames();
}
