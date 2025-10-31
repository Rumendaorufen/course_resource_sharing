package com.course.controller;

import com.course.common.annotation.RequireRole;
import com.course.common.api.ApiResult;
import com.course.dto.CourseDTO;
import com.course.entity.Course;
import com.course.security.UserDetailsImpl;
import com.course.service.CourseService;
import com.course.vo.CourseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "课程管理接口", description = "课程的增删改查接口")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @RequireRole("ADMIN")
    @Operation(summary = "创建新课程", description = "管理员创建新的课程")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "课程创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权创建课程")
    })
    public ApiResult<Void> createCourse(@Validated @RequestBody @Parameter(description = "课程信息", required = true) CourseDTO courseDTO) {
        courseService.createCourse(courseDTO);
        return ApiResult.success();
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    @Operation(summary = "更新课程信息", description = "管理员更新课程信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "课程更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权更新课程"),
        @ApiResponse(responseCode = "404", description = "课程不存在")
    })
    public ApiResult<Void> updateCourse(@PathVariable @Parameter(description = "课程ID", required = true) Long id,
                                      @Validated @RequestBody @Parameter(description = "课程信息", required = true) CourseDTO courseDTO) {
        courseService.updateCourse(id, courseDTO);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    @Operation(summary = "删除课程", description = "管理员删除指定ID的课程")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "课程删除成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权删除此课程"),
        @ApiResponse(responseCode = "404", description = "课程不存在")
    })
    public ApiResult<Void> deleteCourse(@PathVariable @Parameter(description = "课程ID", required = true) Long id) {
        courseService.deleteCourse(id);
        return ApiResult.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情", description = "根据ID获取课程详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取课程详情"),
        @ApiResponse(responseCode = "404", description = "课程不存在")
    })
    public ApiResult<CourseVO> getCourseById(@PathVariable @Parameter(description = "课程ID", required = true) Long id) {
        return ApiResult.success(courseService.getCourseById(id));
    }

    @GetMapping
    @Operation(summary = "获取课程列表", description = "获取所有课程列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ApiResult<List<CourseVO>> getCourses() {
        List<Course> courses = courseService.list();
        List<CourseVO> courseVOs = courses.stream()
            .map(course -> {
                CourseVO vo = new CourseVO();
                BeanUtils.copyProperties(course, vo);
                vo.setTeacherName(course.getTeacherName()); // 设置教师姓名
                return vo;
            })
            .collect(Collectors.toList());
        return ApiResult.success(courseVOs);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有课程列表", description = "获取系统中所有课程的列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ApiResult<List<Course>> getAllCourses() {
        return ApiResult.success(courseService.list());
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "获取教师课程列表", description = "获取指定教师ID的课程列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取课程列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问")
    })
    public ApiResult<List<CourseVO>> getTeacherCourses(@PathVariable @Parameter(description = "教师ID", required = true) Long teacherId) {
        List<Course> courses = courseService.getTeacherCourses(teacherId);
        List<CourseVO> courseVOs = courses.stream().map(course -> {
            CourseVO courseVO = new CourseVO();
            BeanUtils.copyProperties(course, courseVO);
            return courseVO;
        }).collect(Collectors.toList());
        return ApiResult.success(courseVOs);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生课程列表", description = "获取指定学生ID的课程列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取课程列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权访问")
    })
    public ApiResult<List<CourseVO>> getStudentCourses(@PathVariable @Parameter(description = "学生ID", required = true) Long studentId) {
        List<Course> courses = courseService.getStudentCourses(studentId);
        List<CourseVO> courseVOs = courses.stream().map(course -> {
            CourseVO courseVO = new CourseVO();
            BeanUtils.copyProperties(course, courseVO);
            return courseVO;
        }).collect(Collectors.toList());
        return ApiResult.success(courseVOs);
    }

    @PostMapping("/{id}/select")
    @RequireRole("STUDENT")
    @Operation(summary = "学生选课", description = "学生选课")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "选课成功"),
        @ApiResponse(responseCode = "400", description = "已选过此课程"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权选课"),
        @ApiResponse(responseCode = "404", description = "课程不存在")
    })
    public ApiResult<Void> selectCourse(@PathVariable @Parameter(description = "课程ID", required = true) Long id, @RequestParam @Parameter(description = "学生ID", required = true) Long studentId) {
        courseService.selectCourse(studentId, id);
        return ApiResult.success();
    }

    @PostMapping("/{id}/drop")
    @RequireRole("STUDENT")
    @Operation(summary = "学生退课", description = "学生退课")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "退课成功"),
        @ApiResponse(responseCode = "400", description = "未选过此课程"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权退课"),
        @ApiResponse(responseCode = "404", description = "课程不存在")
    })
    public ApiResult<Void> dropCourse(@PathVariable @Parameter(description = "课程ID", required = true) Long id, @RequestParam @Parameter(description = "学生ID", required = true) Long studentId) {
        courseService.dropCourse(studentId, id);
        return ApiResult.success();
    }
}
