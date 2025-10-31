package com.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.course.entity.Assignment;
import com.course.entity.HomeworkSubmission;
import com.course.mapper.AssignmentMapper;
import com.course.mapper.HomeworkSubmissionMapper;
import com.course.service.HomeworkSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkSubmissionServiceImpl extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission>
        implements HomeworkSubmissionService {

    private final AssignmentMapper assignmentMapper;

    @Override
    @Transactional
    public void saveSubmission(HomeworkSubmission submission) {
        submission.setSubmitTime(LocalDateTime.now());
        save(submission);
    }

    @Override
    @Transactional
    public void updateSubmission(HomeworkSubmission submission) {
        submission.setUpdateTime(LocalDateTime.now());
        updateById(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(Long id) {
        removeById(id);
    }

    @Override
    public HomeworkSubmission getSubmissionById(Long id) {
        return getById(id);
    }

    @Override
    public List<HomeworkSubmission> getHomeworkSubmissions(Long assignmentId) {
        return lambdaQuery()
                .eq(HomeworkSubmission::getAssignmentId, assignmentId)
                .orderByDesc(HomeworkSubmission::getSubmitTime)
                .list();
    }

    @Override
    public List<HomeworkSubmission> getSubmissionHistory(Long studentId, Long assignmentId) {
        return lambdaQuery()
                .eq(HomeworkSubmission::getStudentId, studentId)
                .eq(HomeworkSubmission::getAssignmentId, assignmentId)
                .orderByDesc(HomeworkSubmission::getSubmitTime)
                .list();
    }

    @Override
    public HomeworkSubmission getLatestSubmission(Long studentId, Long assignmentId) {
        return lambdaQuery()
                .eq(HomeworkSubmission::getStudentId, studentId)
                .eq(HomeworkSubmission::getAssignmentId, assignmentId)
                .orderByDesc(HomeworkSubmission::getSubmitTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<HomeworkSubmission> getStudentSubmissions(Long studentId) {
        return lambdaQuery()
                .eq(HomeworkSubmission::getStudentId, studentId)
                .orderByDesc(HomeworkSubmission::getSubmitTime)
                .list();
    }

    @Override
    public int countStudentSubmissionsInCourse(Long studentId, Long courseId) {
        // 首先获取该课程的所有作业ID
        List<Long> assignmentIds = assignmentMapper.selectList(
                new LambdaQueryWrapper<Assignment>()
                        .eq(Assignment::getCourseId, courseId)
                        .select(Assignment::getId)
        ).stream().map(Assignment::getId).collect(Collectors.toList());

        if (assignmentIds.isEmpty()) {
            return 0;
        }

        // 然后统计这些作业的提交数
        return lambdaQuery()
                .eq(HomeworkSubmission::getStudentId, studentId)
                .in(HomeworkSubmission::getAssignmentId, assignmentIds)
                .count()
                .intValue();
    }
}
