package com.course.controller;

import com.course.common.annotation.CheckResourceOwner;
import com.course.common.annotation.RequireRole;
import com.course.common.api.ApiResult;
import com.course.dto.AssignmentDTO;
import com.course.entity.Assignment;
import com.course.service.AssignmentService;
import com.course.vo.AssignmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Tag(name = "作业管理接口", description = "作业的发布、更新、删除等接口")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    @RequireRole("TEACHER")
    @Operation(summary = "发布作业", description = "教师发布新作业")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "发布成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权发布作业")
    })
    public ApiResult<Void> createAssignment(
            @Validated @RequestBody @Parameter(description = "作业信息", required = true) AssignmentDTO assignmentDTO) {
        assignmentService.createAssignment(assignmentDTO);
        return ApiResult.success();
    }

    @PutMapping("/{id}")
    @RequireRole("TEACHER")
    @CheckResourceOwner(parameterName = "id", resourceType = "ASSIGNMENT")
    @Operation(summary = "更新作业", description = "更新指定ID的作业信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权更新作业")
    })
    public ApiResult<Void> updateAssignment(
            @PathVariable @Parameter(description = "作业ID", required = true) Long id,
            @Validated @RequestBody @Parameter(description = "更新的作业信息", required = true) AssignmentDTO assignmentDTO) {
        assignmentService.updateAssignment(id, assignmentDTO);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @RequireRole("TEACHER")
    @CheckResourceOwner(parameterName = "id", resourceType = "ASSIGNMENT")
    @Operation(summary = "删除作业", description = "删除指定ID的作业")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权删除作业")
    })
    public ApiResult<Void> deleteAssignment(
            @PathVariable @Parameter(description = "作业ID", required = true) Long id) {
        assignmentService.deleteAssignment(id);
        return ApiResult.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取作业详情", description = "获取指定ID的作业详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问"),
        @ApiResponse(responseCode = "404", description = "作业不存在")
    })
    public ApiResult<AssignmentVO> getAssignmentById(
            @PathVariable @Parameter(description = "作业ID", required = true) Long id) {
        Assignment assignment = assignmentService.getAssignmentById(id);
        if (assignment == null) {
            return ApiResult.error("作业不存在");
        }
        
        AssignmentVO assignmentVO = new AssignmentVO();
        BeanUtils.copyProperties(assignment, assignmentVO);
        assignmentVO.setCourseName(assignment.getCourseName());
        assignmentVO.setTeacherName(assignment.getTeacherName());
        
        return ApiResult.success(assignmentVO);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程作业列表", description = "获取指定课程的所有作业")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问"),
        @ApiResponse(responseCode = "404", description = "课程不存在")
    })
    public ApiResult<List<AssignmentVO>> getAssignmentsByCourseId(
            @PathVariable @Parameter(description = "课程ID", required = true) Long courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourseId(courseId);
        if (assignments == null || assignments.isEmpty()) {
            return ApiResult.success(Collections.emptyList());
        }
        
        List<AssignmentVO> assignmentVOs = assignments.stream()
            .map(assignment -> {
                AssignmentVO vo = new AssignmentVO();
                BeanUtils.copyProperties(assignment, vo);
                vo.setCourseName(assignment.getCourseName());
                vo.setTeacherName(assignment.getTeacherName());
                return vo;
            })
            .collect(Collectors.toList());
            
        return ApiResult.success(assignmentVOs);
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "获取教师作业列表", description = "获取指定教师的所有作业")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问"),
        @ApiResponse(responseCode = "404", description = "教师不存在")
    })
    public ApiResult<List<AssignmentVO>> getTeacherAssignments(
            @PathVariable @Parameter(description = "教师ID", required = true) Long teacherId) {
        List<Assignment> assignmentList = assignmentService.getAssignmentsByTeacherId(teacherId);
        List<AssignmentVO> assignmentVOs = assignmentList.stream().map(assignment -> {
            AssignmentVO assignmentVO = new AssignmentVO();
            BeanUtils.copyProperties(assignment, assignmentVO);
            assignmentVO.setCourseName(assignment.getCourseName());
            assignmentVO.setTeacherName(assignment.getTeacherName());
            return assignmentVO;
        }).collect(Collectors.toList());
        return ApiResult.success(assignmentVOs);
    }

    @GetMapping
    @Operation(summary = "获取作业列表", description = "根据查询参数获取作业列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问")
    })
    public ApiResult<List<AssignmentVO>> getAssignments(
            @RequestParam(required = false) @Parameter(description = "教师ID") Long teacherId,
            @RequestParam(required = false) @Parameter(description = "课程ID") Long courseId,
            @RequestParam(required = false) @Parameter(description = "学生ID") Long studentId) {
        
        List<Assignment> assignmentList;
        if (teacherId != null) {
            assignmentList = assignmentService.getAssignmentsByTeacherId(teacherId);
        } else if (courseId != null) {
            assignmentList = assignmentService.getAssignmentsByCourseId(courseId);
        } else if (studentId != null) {
            assignmentList = assignmentService.getAssignmentsByStudentId(studentId);
        } else {
            // 如果没有任何参数，返回空列表
            assignmentList = Collections.emptyList();
        }

        List<AssignmentVO> assignmentVOs = assignmentList.stream().map(assignment -> {
            AssignmentVO assignmentVO = new AssignmentVO();
            BeanUtils.copyProperties(assignment, assignmentVO);
            assignmentVO.setCourseName(assignment.getCourseName());
            assignmentVO.setTeacherName(assignment.getTeacherName());
            return assignmentVO;
        }).collect(Collectors.toList());
        
        return ApiResult.success(assignmentVOs);
    }

    @GetMapping("/{id}/deadline-passed")
    @Operation(summary = "检查作业是否过期", description = "检查指定ID的作业是否过期")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "检查成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问"),
        @ApiResponse(responseCode = "404", description = "作业不存在")
    })
    public ApiResult<Boolean> isAssignmentDeadlinePassed(
            @PathVariable @Parameter(description = "作业ID", required = true) Long id) {
        boolean isPassed = assignmentService.isAssignmentDeadlinePassed(id);
        return ApiResult.success(isPassed);
    }
}
