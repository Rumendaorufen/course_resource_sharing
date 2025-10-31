package com.course.controller;

import com.course.common.ApiResult;
import com.course.constant.SubmissionStatus;
import com.course.dto.HomeworkSubmissionDTO;
import com.course.entity.Assignment;
import com.course.entity.HomeworkSubmission;
import com.course.entity.User;
import com.course.service.AssignmentService;
import com.course.service.HomeworkSubmissionService;
import com.course.service.UserService;
import com.course.utils.SecurityUtils;
import com.course.vo.HomeworkSubmissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "作业提交接口")
@Slf4j
@RestController
@RequestMapping("/api/homework-submissions")
@RequiredArgsConstructor
public class HomeworkSubmissionController {

    private final HomeworkSubmissionService submissionService;
    private final UserService userService;
    private final AssignmentService assignmentService;

    @Operation(summary = "学生提交作业")
    @PostMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<Void> submitHomework(
            @Validated @RequestBody HomeworkSubmissionDTO submissionDTO,
            @PathVariable @Parameter(description = "学生ID") Long studentId) {
        // 安全检查：确保当前用户只能提交自己的作业
        User currentUser = SecurityUtils.getCurrentUser();
        if (!currentUser.getId().equals(studentId)) {
            return ApiResult.error("无权提交其他学生的作业");
        }

        // 检查作业是否存在且未截止
        Assignment assignment = assignmentService.getAssignmentById(submissionDTO.getAssignmentId());
        if (assignment == null) {
            return ApiResult.error("作业不存在");
        }

        // 检查是否已截止
        boolean isLate = assignmentService.isAssignmentDeadlinePassed(assignment.getId());
        if (isLate) {
            return ApiResult.error("作业已截止，无法提交");
        }

        // 检查附件大小（如果有）
        if (StringUtils.hasText(submissionDTO.getAttachmentUrl()) && submissionDTO.getAttachmentSize() > 10 * 1024 * 1024) {
            return ApiResult.error("附件大小不能超过10MB");
        }

        // 保存提交记录
        HomeworkSubmission submission = new HomeworkSubmission();
        BeanUtils.copyProperties(submissionDTO, submission);
        submission.setStudentId(studentId);
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setCreateTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());

        submissionService.saveSubmission(submission);
        return ApiResult.success(null);
    }

    @Operation(summary = "更新作业提交")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<Void> updateSubmission(
            @PathVariable @Parameter(description = "提交ID") Long id,
            @Validated @RequestBody HomeworkSubmissionDTO submissionDTO) {
        HomeworkSubmission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ApiResult.error("提交记录不存在");
        }

        // 安全检查：只能更新自己的提交
        User currentUser = SecurityUtils.getCurrentUser();
        if (!submission.getStudentId().equals(currentUser.getId())) {
            return ApiResult.error("无权修改其他学生的提交");
        }

        // 检查是否已截止
        if (assignmentService.isAssignmentDeadlinePassed(submission.getAssignmentId())) {
            return ApiResult.error("作业已截止，无法修改");
        }

        // 检查是否已批改
        if (SubmissionStatus.GRADED.equals(submission.getStatus())) {
            return ApiResult.error("作业已批改，无法修改");
        }

        // 更新提交内容
        BeanUtils.copyProperties(submissionDTO, submission);
        submission.setUpdateTime(LocalDateTime.now());
        submissionService.updateSubmission(submission);

        return ApiResult.success(null);
    }

    @Operation(summary = "删除作业提交")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResult<Void> deleteSubmission(@PathVariable @Parameter(description = "提交ID") Long id) {
        HomeworkSubmission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ApiResult.error("提交记录不存在");
        }

        // 安全检查：学生只能删除自己的提交，教师可以删除所有提交
        User currentUser = SecurityUtils.getCurrentUser();
        if (!SecurityUtils.hasRole("TEACHER") && !submission.getStudentId().equals(currentUser.getId())) {
            return ApiResult.error("无权删除其他学生的提交");
        }

        // 检查是否已批改
        if (SubmissionStatus.GRADED.equals(submission.getStatus()) && !SecurityUtils.hasRole("TEACHER")) {
            return ApiResult.error("作业已批改，无法删除");
        }

        submissionService.deleteSubmission(id);
        return ApiResult.success(null);
    }

    @Operation(summary = "批改作业")
    @PutMapping("/{id}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<Void> gradeSubmission(
            @PathVariable Long id,
            @RequestParam Integer score,
            @RequestParam(required = false) String comment) {
        // 验证提交记录是否存在
        HomeworkSubmission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ApiResult.error("提交记录不存在");
        }

        // 更新分数和评语
        submission.setScore(score);
        submission.setComment(comment);
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradeTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());

        submissionService.updateSubmission(submission);
        return ApiResult.success(null);
    }

    @Operation(summary = "获取作业提交详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResult<HomeworkSubmissionVO> getSubmission(@PathVariable @Parameter(description = "提交ID") Long id) {
        HomeworkSubmission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ApiResult.error("提交记录不存在");
        }

        // 安全检查：学生只能查看自己的提交，教师可以查看所有提交
        User currentUser = SecurityUtils.getCurrentUser();
        if (!SecurityUtils.hasRole("TEACHER") && !submission.getStudentId().equals(currentUser.getId())) {
            return ApiResult.error("无权查看其他学生的提交");
        }

        return ApiResult.success(convertToVO(submission));
    }

    @Operation(summary = "获取作业的所有提交")
    @GetMapping("/homework/{homeworkId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<List<HomeworkSubmissionVO>> getHomeworkSubmissions(
            @PathVariable @Parameter(description = "作业ID") Long homeworkId) {
        List<HomeworkSubmission> submissions = submissionService.getHomeworkSubmissions(homeworkId);
        List<HomeworkSubmissionVO> submissionVOs = submissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return ApiResult.success(submissionVOs);
    }

    private HomeworkSubmissionVO convertToVO(HomeworkSubmission submission) {
        if (submission == null) {
            return null;
        }
        HomeworkSubmissionVO vo = new HomeworkSubmissionVO();
        BeanUtils.copyProperties(submission, vo);

        // 获取作业标题
        Assignment assignment = assignmentService.getAssignmentById(submission.getAssignmentId());
        if (assignment != null) {
            vo.setAssignmentTitle(assignment.getTitle());
        }

        // 获取学生姓名
        User student = userService.getById(submission.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getUsername());
        }

        return vo;
    }

    @Operation(summary = "获取学生作业提交历史")
    @GetMapping("/student/{studentId}/assignment/{assignmentId}/history")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResult<List<HomeworkSubmissionVO>> getSubmissionHistory(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId) {
        // 安全检查
        String currentUsername = SecurityUtils.getCurrentUser().getUsername();
        User currentUser = userService.findByUsername(currentUsername);
        if (!currentUser.getId().equals(studentId)) {
            return ApiResult.error("无权查看其他学生的提交记录");
        }

        List<HomeworkSubmission> submissions = submissionService.getSubmissionHistory(studentId, assignmentId);
        List<HomeworkSubmissionVO> vos = submissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return ApiResult.success(vos);
    }

    @Operation(summary = "获取学生最新提交")
    @GetMapping("/student/{studentId}/assignment/{assignmentId}/latest")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResult<HomeworkSubmissionVO> getLatestSubmission(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId) {
        // 安全检查
        String currentUsername = SecurityUtils.getCurrentUser().getUsername();
        User currentUser = userService.findByUsername(currentUsername);
        if (!currentUser.getId().equals(studentId)) {
            return ApiResult.error("无权查看其他学生的提交记录");
        }

        HomeworkSubmission submission = submissionService.getLatestSubmission(studentId, assignmentId);
        if (submission == null) {
            return ApiResult.success(null);
        }
        return ApiResult.success(convertToVO(submission));
    }

    @Operation(summary = "获取学生所有提交")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResult<List<HomeworkSubmissionVO>> getStudentSubmissions(
            @PathVariable Long studentId) {
        // 安全检查
        String currentUsername = SecurityUtils.getCurrentUser().getUsername();
        User currentUser = userService.findByUsername(currentUsername);
        if (!currentUser.getId().equals(studentId)) {
            return ApiResult.error("无权查看其他学生的提交记录");
        }

        List<HomeworkSubmission> submissions = submissionService.getStudentSubmissions(studentId);
        List<HomeworkSubmissionVO> vos = submissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return ApiResult.success(vos);
    }

    @Operation(summary = "统计学生在课程中的提交次数")
    @GetMapping("/count/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResult<Integer> countStudentSubmissionsInCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        // 安全检查
        String currentUsername = SecurityUtils.getCurrentUser().getUsername();
        User currentUser = userService.findByUsername(currentUsername);
        if (!currentUser.getId().equals(studentId) && !SecurityUtils.hasRole("TEACHER")) {
            return ApiResult.error("无权查看其他学生的提交统计");
        }

        int count = submissionService.countStudentSubmissionsInCourse(studentId, courseId);
        return ApiResult.success(count);
    }
}
