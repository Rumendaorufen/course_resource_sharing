package com.course.job;

import com.course.entity.MongoCleanup;
import com.course.mapper.MongoCleanupMapper;
import com.course.service.MongoDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB清理任务，用于清理MongoDB中的垃圾数据
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MongoCleanupJob {

    private final MongoCleanupMapper mongoCleanupMapper;
    private final MongoDbService mongoDbService;

    /**
     * 每小时执行一次，清理MongoDB垃圾数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupMongoData() {
        log.info("Starting MongoDB cleanup job...");
        
        // 1. 查询待清理记录
        List<MongoCleanup> cleanupList = mongoCleanupMapper.selectByStatus(0);
        log.info("Found {} records to cleanup", cleanupList.size());
        
        // 2. 遍历清理记录
        for (MongoCleanup cleanup : cleanupList) {
            try {
                // 3. 删除MongoDB文档
                boolean deleted = mongoDbService.deleteDocument(cleanup.getMongoId(), cleanup.getCollection());
                
                if (deleted) {
                    // 4. 更新清理记录状态
                    cleanup.setStatus(1); // 已清理
                    cleanup.setCleanupTime(LocalDateTime.now());
                    mongoCleanupMapper.updateById(cleanup);
                    
                    log.info("Successfully cleaned up MongoDB document: mongoId: {}, collection: {}",
                            cleanup.getMongoId(), cleanup.getCollection());
                } else {
                    log.error("Failed to clean up MongoDB document: mongoId: {}, collection: {}",
                            cleanup.getMongoId(), cleanup.getCollection());
                }
            } catch (Exception e) {
                log.error("Error during MongoDB cleanup: {}, mongoId: {}, collection: {}",
                        e.getMessage(), cleanup.getMongoId(), cleanup.getCollection());
            }
        }
        
        log.info("MongoDB cleanup job completed. Processed {} records.", cleanupList.size());
    }
}