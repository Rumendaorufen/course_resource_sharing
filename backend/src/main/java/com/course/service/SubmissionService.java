package com.course.service;

import com.course.dto.HomeworkSubmissionDTO;
import com.course.entity.HomeworkSubmission;
import java.util.List;

/**
 * 作业提交服务接口
 */
public interface SubmissionService {
    
    /**
     * 保存作业提交
     * @param submissionDTO 作业提交实体
     */
    void saveSubmission(HomeworkSubmissionDTO submissionDTO);

    /**
     * 更新作业提交
     * @param id 作业提交ID
     * @param submissionDTO 作业提交实体
     */
    void updateSubmission(Long id, HomeworkSubmissionDTO submissionDTO);

    /**
     * 根据ID获取作业提交
     * @param id 提交ID
     * @return 作业提交实体
     */
    HomeworkSubmission getSubmissionById(Long id);

    /**
     * 获取学生在某个作业的提交记录
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @return 作业提交实体
     */
    HomeworkSubmission getSubmissionByAssignmentAndStudent(Long assignmentId, Long studentId);

    /**
     * 获取作业的所有提交记录
     * @param assignmentId 作业ID
     * @return 提交记录列表
     */
    List<HomeworkSubmission> getSubmissionsByAssignment(Long assignmentId);

    /**
     * 获取学生的所有提交记录
     * @param studentId 学生ID
     * @return 提交记录列表
     */
    List<HomeworkSubmission> getSubmissionsByStudent(Long studentId);

    /**
     * 获取学生的所有提交记录
     * @param studentId 学生ID
     * @return 提交记录列表
     */
    List<HomeworkSubmission> getSubmissionsByStudentId(Long studentId);

    /**
     * 删除作业提交
     * @param id 提交ID
     */
    void deleteSubmission(Long id);
}
