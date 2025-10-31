package com.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.course.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {
    
    List<Assignment> findByCourseId(Long courseId);
    
    List<Assignment> findByTeacherId(Long teacherId);
    
    List<Assignment> findByStudentId(Long studentId);
}
