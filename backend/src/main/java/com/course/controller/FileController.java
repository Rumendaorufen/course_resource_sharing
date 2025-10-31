package com.course.controller;

import com.course.common.ApiResult;
import com.course.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件上传接口", description = "处理文件上传和下载的接口")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!fileService.isAllowedFileType(file.getOriginalFilename())) {
                return ApiResult.error("不支持的文件类型");
            }

            String filePath = fileService.storeFile(file);
            String fileUrl = fileService.getFileUrl(filePath);
            return ApiResult.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ApiResult.error("文件上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/download/{filename:.+}")
    @Operation(summary = "下载文件")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(fileService.getFileUrl(filename));
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
