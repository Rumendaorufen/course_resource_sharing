package com.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.course.document.SubmissionDocument;
import com.course.entity.Assignment;
import com.course.entity.HomeworkSubmission;
import com.course.mapper.AssignmentMapper;
import com.course.mapper.HomeworkSubmissionMapper;
import com.course.service.MongoDbService;
import com.course.service.HomeworkSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkSubmissionServiceImpl extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission>
        implements HomeworkSubmissionService {

    private final AssignmentMapper assignmentMapper;
    private final MongoDbService mongoDbService;

    @Override
    @Transactional
    public void saveSubmission(HomeworkSubmission submission) {
        // 生成mongoId
        String mongoId = UUID.randomUUID().toString();
        log.info("生成mongoId: {}", mongoId);
        
        submission.setSubmitTime(LocalDateTime.now());
        submission.setCreateTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());
        submission.setMongoId(mongoId); // 设置mongoId
        
        // 转换为MongoDB文档
        SubmissionDocument submissionDoc = new SubmissionDocument();
        submissionDoc.setId(mongoId);
        submissionDoc.setSubmissionId(submission.getId());
        submissionDoc.setAssignmentId(submission.getAssignmentId());
        submissionDoc.setStudentId(submission.getStudentId());
        submissionDoc.setStudentName(submission.getStudentName());
        submissionDoc.setContent(submission.getContent());
        
        // 设置附件信息
        SubmissionDocument.Attachment attachment = new SubmissionDocument.Attachment();
        attachment.setUrl(submission.getAttachmentUrl());
        attachment.setName(submission.getAttachmentName());
        attachment.setSize(submission.getAttachmentSize());
        submissionDoc.setAttachment(attachment);
        
        submissionDoc.setStatus(submission.getStatus());
        submissionDoc.setScore(submission.getScore());
        submissionDoc.setComment(submission.getComment());
        submissionDoc.setFeedback(submission.getFeedback());
        submissionDoc.setSubmitTime(submission.getSubmitTime());
        submissionDoc.setGradeTime(submission.getGradeTime());
        submissionDoc.setCreateTime(submission.getCreateTime());
        submissionDoc.setUpdateTime(submission.getUpdateTime());
        
        // 写入MongoDB
        mongoDbService.saveSubmission(submissionDoc);
        log.info("作业提交写入MongoDB成功, mongoId: {}", mongoId);
        
        // 写入MySQL
        save(submission);
        log.info("作业提交保存成功, ID: {}", submission.getId());
        
        // 更新MongoDB文档的submissionId
        submissionDoc.setSubmissionId(submission.getId());
        mongoDbService.saveSubmission(submissionDoc);
        log.info("更新MongoDB文档submissionId成功, submissionId: {}", submission.getId());
    }

    @Override
    @Transactional
    public void updateSubmission(HomeworkSubmission submission) {
        // 获取旧的提交信息，用于获取旧mongoId
        HomeworkSubmission oldSubmission = getById(submission.getId());
        if (oldSubmission == null) {
            throw new RuntimeException("作业提交不存在");
        }
        
        // 生成新的mongoId
        String newMongoId = UUID.randomUUID().toString();
        log.info("生成新mongoId: {}", newMongoId);
        
        // 获取旧mongoId
        String oldMongoId = oldSubmission.getMongoId();
        log.info("获取旧mongoId: {}", oldMongoId);
        
        // 更新提交信息
        submission.setUpdateTime(LocalDateTime.now());
        submission.setMongoId(newMongoId); // 设置新的mongoId
        
        // 转换为MongoDB文档
        SubmissionDocument submissionDoc = new SubmissionDocument();
        submissionDoc.setId(newMongoId);
        submissionDoc.setSubmissionId(submission.getId());
        submissionDoc.setAssignmentId(submission.getAssignmentId());
        submissionDoc.setStudentId(submission.getStudentId());
        submissionDoc.setStudentName(submission.getStudentName());
        submissionDoc.setContent(submission.getContent());
        
        // 设置附件信息
        SubmissionDocument.Attachment attachment = new SubmissionDocument.Attachment();
        attachment.setUrl(submission.getAttachmentUrl());
        attachment.setName(submission.getAttachmentName());
        attachment.setSize(submission.getAttachmentSize());
        submissionDoc.setAttachment(attachment);
        
        submissionDoc.setStatus(submission.getStatus());
        submissionDoc.setScore(submission.getScore());
        submissionDoc.setComment(submission.getComment());
        submissionDoc.setFeedback(submission.getFeedback());
        submissionDoc.setSubmitTime(submission.getSubmitTime());
        submissionDoc.setGradeTime(submission.getGradeTime());
        submissionDoc.setCreateTime(submission.getCreateTime());
        submissionDoc.setUpdateTime(submission.getUpdateTime());
        
        // 写入MongoDB
        mongoDbService.saveSubmission(submissionDoc);
        log.info("作业提交更新写入MongoDB成功, mongoId: {}", newMongoId);
        
        // 更新MySQL
        updateById(submission);
        log.info("作业提交更新成功, ID: {}", submission.getId());
        
        // 删除旧MongoDB文档，带重试
        mongoDbService.deleteDocumentWithRetry(oldMongoId, "homework_submission", 3);
        log.info("删除旧MongoDB文档, oldMongoId: {}", oldMongoId);
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
