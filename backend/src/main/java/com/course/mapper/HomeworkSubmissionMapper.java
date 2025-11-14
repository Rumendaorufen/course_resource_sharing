package com.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.course.entity.HomeworkSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HomeworkSubmissionMapper extends BaseMapper<HomeworkSubmission> {
    
    @Select("SELECT * FROM homework_submission WHERE assignment_id = #{assignmentId}")
    List<HomeworkSubmission> findByAssignmentId(Long assignmentId);
    
    @Select("SELECT hs.*, a.title as homework_title, a.description as homework_description, " +
            "a.deadline, c.name as course_name " +
            "FROM homework_submission hs " +
            "LEFT JOIN assignment a ON hs.assignment_id = a.id " +
            "LEFT JOIN course c ON a.course_id = c.id " +
            "WHERE hs.student_id = #{studentId}")
    List<HomeworkSubmission> findByStudentId(Long studentId);
    
    @Select("SELECT hs.* FROM homework_submission hs " +
            "INNER JOIN assignment a ON hs.assignment_id = a.id " +
            "WHERE hs.student_id = #{studentId}")
    List<HomeworkSubmission> findByStudentId2(Long studentId);
    
    @Select("SELECT id, assignment_id, student_id, student_name, content, " +
            "attachment_url, attachment_name, attachment_size, status, score, comment, feedback, " +
            "submit_time, grade_time, create_time, update_time " +
            "FROM homework_submission " +
            "WHERE assignment_id = #{assignmentId} AND student_id = #{studentId} " +
            "ORDER BY submit_time DESC LIMIT 1")
    HomeworkSubmission findLatestSubmission(Long assignmentId, Long studentId);
    
    @Update("UPDATE homework_submission SET score = #{score}, comment = #{comment}, " +
            "grade_time = CURRENT_TIMESTAMP, status = 'GRADED' WHERE id = #{id}")
    int updateScore(Long id, Integer score, String comment);
    
    @Select("SELECT COUNT(*) FROM homework_submission hs " +
            "LEFT JOIN assignment a ON hs.assignment_id = a.id " +
            "WHERE hs.student_id = #{studentId} AND a.course_id = #{courseId}")
    int countByCourseAndStudent(Long courseId, Long studentId);
}
