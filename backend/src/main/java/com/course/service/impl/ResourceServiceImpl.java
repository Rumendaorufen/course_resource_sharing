package com.course.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.common.exception.FileOperationException;
import com.course.common.exception.ResourceNotFoundException;
import com.course.common.exception.ServiceException;
import com.course.dto.ResourceDTO;
import com.course.entity.Resource;
import com.course.mapper.ResourceMapper;
import com.course.service.CourseService;
import com.course.service.FileService;
import com.course.service.ResourceService;
import com.course.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final CourseService courseService;
    private final FileService fileService;

    private static final String RESOURCE_CACHE_NAME = "resourceCache";

    @Value("${file.upload.base-path}")
    private String uploadDir;

    @Override
    @Transactional
    @CacheEvict(value = RESOURCE_CACHE_NAME, allEntries = true)
    public Resource uploadResource(ResourceDTO resourceDTO, MultipartFile file, Long uploaderId) {
        log.info("开始上传资源, 课程ID: {}, 上传者ID: {}, 文件名: {}", 
            resourceDTO.getCourseId(), uploaderId, file.getOriginalFilename());
        try {
            // 验证课程存在性
            CourseVO courseVO = courseService.getCourseById(resourceDTO.getCourseId());
            if (courseVO == null) {
                throw new ResourceNotFoundException("Course", "id", resourceDTO.getCourseId());
            }
            log.info("课程验证通过, 课程名称: {}", courseVO.getName());

            // 保存文件并获取文件路径
            String filePath = fileService.storeFile(file);
            log.info("文件保存成功, 路径: {}", filePath);

            // 创建资源记录
            Resource resource = new Resource();
            BeanUtils.copyProperties(resourceDTO, resource);
            resource.setUploaderUserId(uploaderId);
            resource.setFilePath(filePath);
            resource.setFileName(file.getOriginalFilename());
            resource.setFileSize(file.getSize());
            resource.setDownloadCount(0);
            resource.setCreateTime(LocalDateTime.now());
            resource.setUpdateTime(LocalDateTime.now());
            resource.setStatus(1); // Set status to enabled
            
            log.info("准备插入资源记录: {}", resource);
            int result = resourceMapper.insert(resource);
            log.info("资源记录插入结果: {}, 资源ID: {}", result, resource.getId());
            
            if (result <= 0) {
                // 如果插入失败，删除已上传的文件
                fileService.deleteFile(filePath);
                throw new ServiceException("资源记录插入失败");
            }
            
            log.info("资源上传成功, 资源ID: {}", resource.getId());
            return resource;
        } catch (Exception e) {
            log.error("资源上传失败", e);
            throw new FileOperationException("资源上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = RESOURCE_CACHE_NAME, allEntries = true)
    public Resource downloadResource(Long id) {
        log.info("开始下载资源, 资源ID: {}", id);
        try {
            Resource resource = getResourceById(id);
            if (resource == null) {
                log.error("资源不存在, 资源ID: {}", id);
                throw new ResourceNotFoundException("Resource", "id", id);
            }

            // 只更新下载次数
            resourceMapper.incrementDownloadCount(id);
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            log.debug("更新下载次数成功, 资源ID: {}, 当前下载次数: {}", id, resource.getDownloadCount());

            return resource;
        } catch (Exception e) {
            log.error("资源下载失败, 资源ID: {}", id, e);
            throw new FileOperationException("资源下载失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = RESOURCE_CACHE_NAME, allEntries = true)
    public void deleteResource(Long id) {
        log.info("开始删除资源, 资源ID: {}", id);
        try {
            Resource resource = getResourceById(id);
            if (resource == null) {
                log.error("资源不存在, 资源ID: {}", id);
                throw new ResourceNotFoundException("Resource", "id", id);
            }

            // 删除文件
            fileService.deleteFile(resource.getFilePath());
            log.debug("文件删除成功, 路径: {}", resource.getFilePath());

            // 删除资源记录
            resourceMapper.deleteById(id);
            log.info("资源删除成功, 资源ID: {}", id);
        } catch (Exception e) {
            log.error("资源删除失败, 资源ID: {}", id, e);
            throw new FileOperationException("资源删除失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getResourceById(Long id) {
        log.debug("查询资源, 资源ID: {}", id);
        Resource resource = resourceMapper.findByIdWithDetails(id);
        if (resource == null) {
            log.warn("资源不存在, 资源ID: {}", id);
        }
        return resource;
    }

    @Override
    public List<Resource> getCourseResources(Long courseId) {
        log.debug("查询课程资源列表, 课程ID: {}", courseId);
        return resourceMapper.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resource> getUserResources(Long userId) {
        log.debug("查询用户资源列表, 用户ID: {}", userId);
        return resourceMapper.findByUploaderId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<Resource> getResourcesByDownloads(int page, int size) {
        log.debug("查询热门资源列表, 页码: {}, 每页数量: {}", page, size);
        return resourceMapper.selectPage(
            new Page<>(page, size),
            null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resource> searchResources(String keyword) {
        log.debug("搜索资源, 关键词: {}", keyword);
        return resourceMapper.searchByName(keyword);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        resourceMapper.incrementDownloadCount(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = RESOURCE_CACHE_NAME, allEntries = true)
    public void updateResource(Long id, ResourceDTO resourceDTO) {
        log.info("开始更新资源, 资源ID: {}", id);
        try {
            Resource resource = getResourceById(id);
            if (resource == null) {
                log.error("资源不存在, 资源ID: {}", id);
                throw new ResourceNotFoundException("Resource", "id", id);
            }

            resource.setName(resourceDTO.getName());
            resource.setDescription(resourceDTO.getDescription());
            resource.setType(resourceDTO.getType());

            if (resourceMapper.updateById(resource) != 1) {
                throw new ServiceException("更新资源信息失败");
            }
            log.info("资源更新成功, 资源ID: {}", id);
        } catch (Exception e) {
            log.error("资源更新失败, 资源ID: {}", id, e);
            throw new ServiceException("资源更新失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = RESOURCE_CACHE_NAME, key = "'allResources'")
    public List<Resource> getAllResources() {
        try {
            List<Resource> resources = resourceMapper.getAllResourcesWithDetails();
            log.info("从数据库获取资源列表, 数量: {}", resources.size());
            return resources;
        } catch (Exception e) {
            log.error("获取资源列表失败", e);
            throw new ServiceException("获取资源列表失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = RESOURCE_CACHE_NAME, allEntries = true)
    public void clearResourceCache() {
        log.info("清除资源列表缓存");
    }
}
