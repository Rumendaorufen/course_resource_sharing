package com.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.course.common.ApiResult;
import com.course.common.annotation.CheckResourceOwner;
import com.course.common.annotation.RequireRole;
import com.course.common.exception.ResourceNotFoundException;
import com.course.common.exception.FileOperationException;
import com.course.common.exception.UnauthorizedException;
import com.course.dto.ResourceDTO;
import com.course.entity.Course;
import com.course.entity.Resource;
import com.course.entity.User;
import com.course.security.SecurityUtils;
import com.course.service.CourseService;
import com.course.service.ResourceService;
import com.course.service.UserService;
import com.course.vo.CourseVO;
import com.course.vo.ResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/resource")
@Tag(name = "资源管理", description = "资源管理相关接口")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, 
    RequestMethod.DELETE, RequestMethod.OPTIONS
})
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;
    private final CourseService courseService;
    private final UserService userService;
    private final ResourceLoader resourceLoader;

    @Value("${file.upload.base-path}")
    private String uploadDir;

    @PostMapping
    @RequireRole({"TEACHER", "ADMIN", "STUDENT"})
    @Operation(summary = "上传资源", description = "上传新的资源文件")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public ApiResult<Resource> uploadResource(@RequestParam("file") MultipartFile file,
                                            @RequestParam("name") String name,
                                            @RequestParam("description") String description,
                                            @RequestParam("courseId") Long courseId) {
        try {
            log.info("开始上传资源文件: {}", name);
            
            ResourceDTO resourceDTO = new ResourceDTO();
            resourceDTO.setName(name);
            resourceDTO.setDescription(description);
            resourceDTO.setCourseId(courseId);
            
            Long currentUserId = getCurrentUserId();
            Resource resource = resourceService.uploadResource(resourceDTO, file, currentUserId);
            
            log.info("资源文件上传成功: {}", name);
            return ApiResult.success(resource);
        } catch (Exception e) {
            log.error("资源文件上传失败", e);
            return ApiResult.error("资源文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载资源", description = "根据资源ID下载资源文件")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "下载成功"),
        @ApiResponse(responseCode = "404", description = "资源不存在"),
        @ApiResponse(responseCode = "500", description = "下载失败")
    })
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable Long id) {
        try {
            log.info("开始下载资源, 资源ID: {}", id);
            Resource resource = resourceService.downloadResource(id);
            if (resource == null) {
                log.error("资源不存在, 资源ID: {}", id);
                throw new ResourceNotFoundException("Resource", "id", id);
            }

            // 获取文件路径
            String filePath = resource.getFilePath();
            if (!StringUtils.hasText(filePath)) {
                log.error("资源文件路径为空, 资源ID: {}", id);
                throw new FileOperationException("资源文件路径为空");
            }

            // 读取文件
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("资源文件不存在, 路径: {}", filePath);
                throw new FileOperationException("资源文件不存在");
            }

            // 创建文件资源
            org.springframework.core.io.Resource fileResource = new FileSystemResource(filePath);

            // 设置响应头
            String fileName = resource.getFileName();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(fileResource);

        } catch (ResourceNotFoundException e) {
            log.error("资源不存在", e);
            throw e;
        } catch (FileOperationException e) {
            log.error("文件操作失败", e);
            throw e;
        } catch (Exception e) {
            log.error("下载资源失败", e);
            throw new FileOperationException("下载资源失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源", description = "删除指定ID的资源。管理员可删除任何资源，其他用户只能删除自己上传的资源")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "无权限删除此资源"),
        @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ApiResult<Void> deleteResource(@PathVariable Long id) {
        try {
            log.info("开始删除资源: {}", id);
            
            // 获取当前用户信息
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                throw new UnauthorizedException("用户未登录或登录已过期");
            }
            
            // 获取资源信息
            Resource resource = resourceService.getResourceById(id);
            if (resource == null) {
                throw new ResourceNotFoundException("Resource", "id", id);
            }
            
            // 检查权限
            if (!"ADMIN".equals(currentUser.getRole()) && 
                !resource.getUploaderUserId().equals(currentUser.getId())) {
                throw new UnauthorizedException("无权删除此资源");
            }
            
            // 执行删除操作
            resourceService.deleteResource(id);
            log.info("资源删除成功");
            return ApiResult.success(null);
        } catch (Exception e) {
            log.error("删除资源失败", e);
            return ApiResult.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    @Operation(summary = "获取资源列表", description = "获取所有资源列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ApiResult<List<ResourceVO>> getResources(@RequestParam(required = false) Long courseId) {
        try {
            log.info("开始获取资源列表, courseId: {}", courseId);
            List<Resource> resources;
            if (courseId != null) {
                resources = resourceService.getCourseResources(courseId);
            } else {
                resources = resourceService.getAllResources();
            }
            log.info("成功获取到 {} 个资源", resources.size());
            
            List<ResourceVO> resourceVOs = resources.stream().map(this::convertToVO).collect(Collectors.toList());
            
            return ApiResult.success(resourceVOs);
        } catch (Exception e) {
            log.error("获取资源列表失败", e);
            return ApiResult.error("获取资源列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    @Operation(summary = "获取课程资源列表", description = "获取指定课程ID的资源列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ApiResult<List<Resource>> getCourseResources(@PathVariable Long courseId) {
        return ApiResult.success(resourceService.getCourseResources(courseId));
    }

    @PutMapping("/{id}")
    @RequireRole("TEACHER")
    @CheckResourceOwner
    @Operation(summary = "更新资源信息", description = "更新指定ID的资源信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ApiResult<Resource> updateResource(@PathVariable Long id, @RequestBody @Validated ResourceDTO resourceDTO) {
        try {
            log.info("开始更新资源: {}", id);
            resourceService.updateResource(id, resourceDTO);
            Resource updatedResource = resourceService.getResourceById(id);
            log.info("资源更新成功");
            return ApiResult.success(updatedResource);
        } catch (Exception e) {
            log.error("更新资源失败", e);
            return ApiResult.error("更新失败：" + e.getMessage());
        }
    }

    private ResourceVO convertToVO(Resource resource) {
        if (resource == null) {
            return null;
        }

        ResourceVO vo = new ResourceVO();
        vo.setId(resource.getId());
        vo.setName(resource.getName());
        vo.setDescription(resource.getDescription());
        vo.setCourseId(resource.getCourseId());
        vo.setType(resource.getType());
        vo.setFileSize(resource.getFileSize());
        vo.setDownloadCount(resource.getDownloadCount());
        vo.setCreateTime(resource.getCreateTime());
        vo.setUpdateTime(resource.getUpdateTime());
        
        // 设置文件名
        vo.setOriginalFileName(resource.getFileName());
        
        // 从JOIN查询中获取的字段
        vo.setCourseName(resource.getCourseName());
        vo.setUploaderName(resource.getUploaderName());
        vo.setUploaderId(resource.getUploaderUserId());

        return vo;
    }

    // 获取当前用户ID的辅助方法
    private Long getCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录或登录已过期");
        }
        return userId;
    }
}
