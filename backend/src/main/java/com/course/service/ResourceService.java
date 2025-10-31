package com.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.course.dto.ResourceDTO;
import com.course.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    /**
     * 上传资源文件
     * @param resourceDTO 资源信息
     * @param file 文件
     * @param uploaderId 上传者ID
     * @return 上传成功的资源对象
     */
    Resource uploadResource(ResourceDTO resourceDTO, MultipartFile file, Long uploaderId);

    /**
     * 下载资源
     */
    Resource downloadResource(Long id);

    /**
     * 删除资源
     */
    void deleteResource(Long id);

    /**
     * 获取资源详情
     */
    Resource getResourceById(Long id);

    /**
     * 获取所有资源
     */
    List<Resource> getAllResources();

    /**
     * 获取课程的所有资源
     */
    List<Resource> getCourseResources(Long courseId);

    /**
     * 获取用户上传的所有资源
     */
    List<Resource> getUserResources(Long userId);

    /**
     * 按下载次数分页查询资源
     */
    IPage<Resource> getResourcesByDownloads(int page, int size);

    /**
     * 搜索资源
     */
    List<Resource> searchResources(String keyword);

    /**
     * 更新资源
     */
    void updateResource(Long id, ResourceDTO resourceDTO);

    /**
     * 增加资源下载次数
     */
    void incrementDownloadCount(Long id);

    /**
     * 清除资源缓存
     */
    void clearResourceCache();
}
