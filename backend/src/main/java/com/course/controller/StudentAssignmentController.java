package com.course.controller;

import com.course.common.ApiResult;
import com.course.constant.SubmissionStatus;
import com.course.dto.HomeworkSubmissionDTO;
import com.course.entity.Assignment;
import com.course.entity.Course;
import com.course.entity.HomeworkSubmission;
import com.course.service.AssignmentService;
import com.course.service.CourseService;
import com.course.service.SubmissionService;
import com.course.utils.SecurityUtils;
import com.course.vo.AssignmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/assignments/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_STUDENT')")
@Tag(name = "学生作业接口", description = "学生查看和提交作业的接口")
public class StudentAssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final CourseService courseService;

    /**
     * 获取学生的作业列表
     */
    @GetMapping("/{studentId}")
    @Operation(summary = "获取学生作业列表", description = "获取指定学生的作业列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问")
    })
    public ApiResult<List<AssignmentVO>> getStudentAssignments(
            @Parameter(description = "学生ID") @PathVariable Long studentId) {
        // 安全检查：确保学生只能查看自己的作业
        if (!SecurityUtils.getCurrentUserId().equals(studentId)) {
            return ApiResult.error("无权查看其他学生的作业");
        }

        List<Assignment> assignments = assignmentService.getAssignmentsByStudentId(studentId);
        List<AssignmentVO> assignmentVOs = assignments.stream().map(assignment -> {
            AssignmentVO vo = new AssignmentVO();
            BeanUtils.copyProperties(assignment, vo);
            
            // 确保课程名称被设置
            if (assignment.getCourseName() == null) {
                vo.setCourseName(courseService.getCourseById(assignment.getCourseId()).getName());
            } else {
                vo.setCourseName(assignment.getCourseName());
            }
            
            // 获取该学生对这个作业的提交记录
            HomeworkSubmission submission = submissionService.getSubmissionByAssignmentAndStudent(assignment.getId(), studentId);
            if (submission != null) {
                vo.setSubmissionStatus(submission.getStatus());
                vo.setSubmissionId(submission.getId());
                vo.setSubmissionTime(submission.getSubmitTime());
                vo.setScore(submission.getScore());
            } else {
                vo.setSubmissionStatus(SubmissionStatus.NOT_SUBMITTED);
                vo.setSubmissionId(null);
                vo.setSubmissionTime(null);
                vo.setScore(null);
            }
            
            return vo;
        }).collect(Collectors.toList());

        return ApiResult.success(assignmentVOs);
    }

    /**
     * 获取作业详情
     */
    @GetMapping("/{studentId}/assignment/{assignmentId}")
    @Operation(summary = "获取作业详情")
    public ApiResult<AssignmentVO> getAssignmentDetail(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId) {
        // 验证当前用户
        if (!SecurityUtils.getCurrentUserId().equals(studentId)) {
            return ApiResult.error("无权访问其他学生的作业");
        }

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            return ApiResult.error("作业不存在");
        }

        // 验证学生是否已选课
        if (!courseService.isStudentEnrolledInCourse(studentId, assignment.getCourseId())) {
            return ApiResult.error("未选修该课程，无法查看作业");
        }

        // 检查截止时间
        LocalDateTime deadline = assignment.getDeadline();
        LocalDateTime now = LocalDateTime.now();
        if (deadline != null && now.isAfter(deadline)) {
            log.info("作业已截止，studentId: {}, assignmentId: {}", studentId, assignmentId);
            return ApiResult.error("作业已截止");
        }

        return ApiResult.success(convertToVO(assignment));
    }

    /**
     * 提交作业
     */
    @PostMapping("/{assignmentId}/submit")
    @Operation(summary = "提交作业", description = "学生提交作业")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "提交成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问")
    })
    public ApiResult<?> submitAssignment(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId,
            @Parameter(description = "作业提交信息") @Valid @RequestBody HomeworkSubmissionDTO submissionDTO) {
        
        // 获取当前学生ID
        Long studentId = SecurityUtils.getCurrentUserId();
        
        // 检查作业是否存在且未过期
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            return ApiResult.error("作业不存在");
        }
        
        if (assignmentService.isAssignmentDeadlinePassed(assignmentId)) {
            return ApiResult.error("作业已过期");
        }

        // 设置提交信息
        submissionDTO.setAssignmentId(assignmentId);
        submissionDTO.setStudentId(studentId);
        submissionDTO.setStatus(SubmissionStatus.SUBMITTED);
        submissionDTO.setSubmitTime(LocalDateTime.now());

        // 保存提交
        submissionService.saveSubmission(submissionDTO);
        
        return ApiResult.success("作业提交成功");
    }

    /**
     * 获取作业提交详情
     */
    @GetMapping("/submission/{submissionId}")
    @Operation(summary = "获取作业提交详情", description = "获取学生作业提交的详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问"),
        @ApiResponse(responseCode = "404", description = "提交记录不存在")
    })
    public ApiResult<HomeworkSubmission> getSubmissionDetail(
            @Parameter(description = "提交ID") @PathVariable Long submissionId) {
        
        HomeworkSubmission submission = submissionService.getSubmissionById(submissionId);
        if (submission == null) {
            return ApiResult.error("提交记录不存在");
        }

        // 安全检查：确保学生只能查看自己的提交
        if (!SecurityUtils.getCurrentUserId().equals(submission.getStudentId())) {
            return ApiResult.error("无权查看其他学生的提交记录");
        }

        return ApiResult.success(submission);
    }

    /**
     * 获取作业提交记录列表
     */
    @GetMapping("/{studentId}/submissions")
    @Operation(summary = "获取作业提交记录列表")
    public ApiResult<List<HomeworkSubmission>> getAssignmentSubmissions(
            @PathVariable Long studentId) {
        // 验证当前用户
        if (!SecurityUtils.getCurrentUserId().equals(studentId)) {
            return ApiResult.error("无权访问其他学生的提交记录");
        }

        List<HomeworkSubmission> submissions = submissionService.getSubmissionsByStudentId(studentId);
        return ApiResult.success(submissions);
    }

    /**
     * 更新提交
     */
    @PutMapping("/{studentId}/submission/{submissionId}")
    @Operation(summary = "更新作业提交")
    public ApiResult<Void> updateSubmission(
            @PathVariable Long studentId,
            @PathVariable Long submissionId,
            @Valid @RequestBody HomeworkSubmissionDTO submissionDTO) {
        // 验证当前用户
        if (!SecurityUtils.getCurrentUserId().equals(studentId)) {
            return ApiResult.error("无权修改其他学生的提交");
        }

        try {
            HomeworkSubmission submission = submissionService.getSubmissionById(submissionId);
            if (submission == null) {
                return ApiResult.error("提交记录不存在");
            }

            // 验证是否为本人的提交
            if (!submission.getStudentId().equals(studentId)) {
                return ApiResult.error("无权修改其他学生的提交");
            }

            // 检查是否已批改
            if (SubmissionStatus.GRADED.equals(submission.getStatus())) {
                return ApiResult.error("已批改的作业不能修改");
            }

            submissionService.updateSubmission(submissionId, submissionDTO);
            log.info("作业更新成功，studentId: {}, submissionId: {}", studentId, submissionId);
            return ApiResult.success(null);
        } catch (Exception e) {
            log.error("作业更新失败", e);
            return ApiResult.error("作业更新失败：" + e.getMessage());
        }
    }

    private AssignmentVO convertToVO(Assignment assignment) {
        AssignmentVO vo = new AssignmentVO();
        BeanUtils.copyProperties(assignment, vo);
        return vo;
    }
}
