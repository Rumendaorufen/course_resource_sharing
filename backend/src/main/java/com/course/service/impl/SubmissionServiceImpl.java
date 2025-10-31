package com.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.course.common.exception.ServiceException;
import com.course.dto.HomeworkSubmissionDTO;
import com.course.entity.HomeworkSubmission;
import com.course.mapper.HomeworkSubmissionMapper;
import com.course.service.SubmissionService;
import com.course.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission> implements SubmissionService {

    private final HomeworkSubmissionMapper submissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSubmission(HomeworkSubmissionDTO submissionDTO) {
        HomeworkSubmission submission = new HomeworkSubmission();
        BeanUtils.copyProperties(submissionDTO, submission);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStudentId(SecurityUtils.getCurrentUserId());
        submissionMapper.insert(submission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubmission(Long id, HomeworkSubmissionDTO submissionDTO) {
        HomeworkSubmission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }

        if (!submission.getStudentId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ServiceException("无权修改此提交记录");
        }

        BeanUtils.copyProperties(submissionDTO, submission);
        submission.setUpdateTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }

    @Override
    public HomeworkSubmission getSubmissionById(Long id) {
        HomeworkSubmission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }
        return submission;
    }

    @Override
    public HomeworkSubmission getSubmissionByAssignmentAndStudent(Long assignmentId, Long studentId) {
        QueryWrapper<HomeworkSubmission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assignment_id", assignmentId)
                   .eq("student_id", studentId)
                   .orderByDesc("submit_time")
                   .last("LIMIT 1");
        return submissionMapper.selectOne(queryWrapper);
    }

    @Override
    public List<HomeworkSubmission> getSubmissionsByAssignment(Long assignmentId) {
        QueryWrapper<HomeworkSubmission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assignment_id", assignmentId)
                   .orderByDesc("submit_time");
        return submissionMapper.selectList(queryWrapper);
    }

    @Override
    public List<HomeworkSubmission> getSubmissionsByStudent(Long studentId) {
        QueryWrapper<HomeworkSubmission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId)
                   .orderByDesc("submit_time");
        return submissionMapper.selectList(queryWrapper);
    }

    @Override
    public List<HomeworkSubmission> getSubmissionsByStudentId(Long studentId) {
        QueryWrapper<HomeworkSubmission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        return submissionMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSubmission(Long id) {
        HomeworkSubmission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }

        if (!submission.getStudentId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ServiceException("无权删除此提交记录");
        }

        submissionMapper.deleteById(id);
    }
}
