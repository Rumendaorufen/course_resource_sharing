package com.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
    
    @Select("SELECT * FROM resource WHERE status = 1 ORDER BY download_count DESC")
    IPage<Resource> pageByDownloadCount(Page<Resource> page);
    
    @Select("SELECT r.*, c.name as course_name, u.username as uploader_name " +
            "FROM resource r " +
            "LEFT JOIN course c ON r.course_id = c.id " +
            "LEFT JOIN user u ON r.uploader_user_id = u.id " +
            "WHERE r.course_id = #{courseId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC")
    List<Resource> findByCourseId(@Param("courseId") Long courseId);
    
    @Select("SELECT r.*, c.name as course_name, u.username as uploader_name " +
            "FROM resource r " +
            "LEFT JOIN course c ON r.course_id = c.id " +
            "LEFT JOIN user u ON r.uploader_user_id = u.id " +
            "WHERE r.uploader_user_id = #{uploaderId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC")
    List<Resource> findByUploaderId(@Param("uploaderId") Long uploaderId);
    
    @Select("SELECT r.* FROM resource r " +
            "WHERE r.title LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR r.description LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY " +
            "CASE WHEN r.title LIKE CONCAT('%', #{keyword}, '%') THEN 1 " +
            "     WHEN r.description LIKE CONCAT('%', #{keyword}, '%') THEN 2 " +
            "END, " +
            "r.download_count DESC, " +
            "r.create_time DESC")
    List<Resource> searchResources(@Param("keyword") String keyword);
    
    @Select("SELECT r.* FROM resource r " +
            "ORDER BY r.download_count DESC, r.create_time DESC " +
            "LIMIT #{limit}")
    List<Resource> findPopularResources(@Param("limit") Integer limit);
    
    @Select("SELECT COUNT(1) FROM resource WHERE course_id = #{courseId}")
    Integer countByCourseId(@Param("courseId") Long courseId);
    
    @Select("SELECT r.* FROM resource r " +
            "WHERE r.course_id = #{courseId} " +
            "AND (r.title LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR r.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY r.create_time DESC")
    IPage<Resource> searchInCourse(IPage<Resource> page, 
                                 @Param("courseId") Long courseId, 
                                 @Param("keyword") String keyword);
    
    @Update("UPDATE resource SET download_count = download_count + 1 WHERE id = #{id}")
    int incrementDownloadCount(@Param("id") Long id);
    
    @Select("SELECT * FROM resource WHERE status = 1 AND name LIKE CONCAT('%', #{keyword}, '%')")
    List<Resource> searchByName(@Param("keyword") String keyword);
    
    /**
     * 查询资源列表，包含上传者姓名和课程名称
     *
     * @param courseId 课程ID，可为null
     * @return 资源列表
     */
    @Select("SELECT r.*, c.name as course_name, u.username as uploader_name " +
            "FROM resource r " +
            "LEFT JOIN course c ON r.course_id = c.id " +
            "LEFT JOIN user u ON r.uploader_user_id = u.id " +
            "WHERE (#{courseId} IS NULL OR r.course_id = #{courseId}) " +
            "AND r.status = 1 " +
            "ORDER BY r.create_time DESC")
    List<Resource> selectResourceList(@Param("courseId") Long courseId);
    
    @Select("SELECT r.*, " +
            "c.name as course_name, " +
            "u.username as uploader_name " +
            "FROM resource r " +
            "LEFT JOIN course c ON r.course_id = c.id " +
            "LEFT JOIN user u ON r.uploader_user_id = u.id " +
            "WHERE r.status = 1 " +
            "ORDER BY r.create_time DESC")
    List<Resource> getAllResourcesWithDetails();
    
    @Select("SELECT r.*, c.name as course_name, u.username as uploader_name " +
            "FROM resource r " +
            "LEFT JOIN course c ON r.course_id = c.id " +
            "LEFT JOIN user u ON r.uploader_user_id = u.id " +
            "WHERE r.id = #{id}")
    Resource findByIdWithDetails(@Param("id") Long id);
}
