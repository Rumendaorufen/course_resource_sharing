package com.course.service;

import com.course.dto.HomeworkSubmissionDTO;
import com.course.entity.HomeworkSubmission;

import java.util.List;

/**
 * 作业提交服务接口
 */
public interface HomeworkSubmissionService {
    /**
     * 保存作业提交
     *
     * @param submission 作业提交实体
     */
    void saveSubmission(HomeworkSubmission submission);

    /**
     * 更新提交
     *
     * @param submission 更新的提交实体
     */
    void updateSubmission(HomeworkSubmission submission);

    /**
     * 删除提交
     *
     * @param id 提交ID
     */
    void deleteSubmission(Long id);

    /**
     * 获取提交详情
     *
     * @param id 提交ID
     * @return 提交详情
     */
    HomeworkSubmission getSubmissionById(Long id);

    /**
     * 获取作业的所有提交
     *
     * @param assignmentId 作业ID
     * @return 提交列表
     */
    List<HomeworkSubmission> getHomeworkSubmissions(Long assignmentId);

    /**
     * 获取学生的提交历史
     *
     * @param studentId 学生ID
     * @param assignmentId 作业ID
     * @return 提交历史列表
     */
    List<HomeworkSubmission> getSubmissionHistory(Long studentId, Long assignmentId);

    /**
     * 获取学生的最新提交
     *
     * @param studentId 学生ID
     * @param assignmentId 作业ID
     * @return 最新提交
     */
    HomeworkSubmission getLatestSubmission(Long studentId, Long assignmentId);

    /**
     * 获取学生的所有提交
     *
     * @param studentId 学生ID
     * @return 提交列表
     */
    List<HomeworkSubmission> getStudentSubmissions(Long studentId);

    /**
     * 统计学生在课程中的提交次数
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 提交次数
     */
    int countStudentSubmissionsInCourse(Long studentId, Long courseId);
}
