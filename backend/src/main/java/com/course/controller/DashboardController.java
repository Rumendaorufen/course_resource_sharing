package com.course.controller;

import com.course.common.api.ApiResult;
import com.course.service.DashboardService;
import com.course.vo.AssignmentVO;
import com.course.vo.ResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "仪表盘接口", description = "获取仪表盘统计数据和最近活动")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据", description = "获取课程、资源、作业的统计数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ApiResult<Map<String, Integer>> getStats() {
        log.debug("Received request for stats");
        try {
            Map<String, Integer> stats = dashboardService.getStats();
            log.debug("Retrieved stats: {}", stats);
            return ApiResult.success(stats);
        } catch (Exception e) {
            log.error("Error getting stats", e);
            return ApiResult.error(e.getMessage());
        }
    }

    @GetMapping("/dashboard/recent-assignments")
    @Operation(summary = "获取最近作业", description = "获取最近的作业列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ApiResult<List<AssignmentVO>> getRecentAssignments() {
        log.debug("Received request for assignments");
        try {
            List<AssignmentVO> assignments = dashboardService.getRecentAssignments();
            log.debug("Retrieved {} assignments", assignments.size());
            return ApiResult.success(assignments);
        } catch (Exception e) {
            log.error("Error getting assignments", e);
            return ApiResult.error(e.getMessage());
        }
    }

    @GetMapping("/resources/recent")
    @Operation(summary = "获取最新资源", description = "获取最新的资源列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ApiResult<List<ResourceVO>> getRecentResources() {
        log.debug("Received request for resources");
        try {
            List<ResourceVO> resources = dashboardService.getRecentResources();
            log.debug("Retrieved {} resources", resources.size());
            return ApiResult.success(resources);
        } catch (Exception e) {
            log.error("Error getting resources", e);
            return ApiResult.error(e.getMessage());
        }
    }
}
