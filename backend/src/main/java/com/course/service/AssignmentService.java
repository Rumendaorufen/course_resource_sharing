package com.course.service;

import com.course.dto.AssignmentDTO;
import com.course.entity.Assignment;
import java.util.List;

public interface AssignmentService {
    /**
     * 发布作业
     */
    void createAssignment(AssignmentDTO assignmentDTO);

    /**
     * 更新作业
     */
    void updateAssignment(Long id, AssignmentDTO assignmentDTO);

    /**
     * 删除作业
     */
    void deleteAssignment(Long id);

    /**
     * 获取作业详情
     */
    Assignment getAssignmentById(Long id);

    /**
     * 获取课程的所有作业
     */
    List<Assignment> getAssignmentsByCourseId(Long courseId);

    /**
     * 获取教师的所有作业
     */
    List<Assignment> getAssignmentsByTeacherId(Long teacherId);

    /**
     * 获取学生的所有作业
     * @param studentId 学生ID
     * @return 作业列表
     */
    List<Assignment> getAssignmentsByStudentId(Long studentId);

    /**
     * 检查作业是否已过截止时间
     */
    boolean isAssignmentDeadlinePassed(Long assignmentId);

    /**
     * 获取所有活动作业
     * @return 活动作业列表
     */
    List<Assignment> getActiveAssignments();
}
