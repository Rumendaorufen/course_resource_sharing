package com.course.service.impl;

import com.course.document.ResourceDocument;
import com.course.document.AssignmentDocument;
import com.course.document.SubmissionDocument;
import com.course.entity.MongoCleanup;
import com.course.mapper.MongoCleanupMapper;
import com.course.service.MongoDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * MongoDB操作服务实现类
 */
@Service
@Slf4j
public class MongoDbServiceImpl implements MongoDbService {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private MongoCleanupMapper mongoCleanupMapper;

    @Override
    public ResourceDocument saveResource(ResourceDocument resourceDoc) {
        return mongoTemplate.save(resourceDoc);
    }

    @Override
    public AssignmentDocument saveAssignment(AssignmentDocument assignmentDoc) {
        return mongoTemplate.save(assignmentDoc);
    }

    @Override
    public SubmissionDocument saveSubmission(SubmissionDocument submissionDoc) {
        return mongoTemplate.save(submissionDoc);
    }

    @Override
    public boolean deleteDocument(String mongoId, String collection) {
        try {
            Query query = Query.query(Criteria.where("_id").is(mongoId));
            mongoTemplate.remove(query, collection);
            return true;
        } catch (Exception e) {
            log.error("Delete MongoDB document failed: {}, mongoId: {}, collection: {}",
                    e.getMessage(), mongoId, collection);
            return false;
        }
    }

    @Override
    public boolean deleteDocumentWithRetry(String mongoId, String collection, int maxRetries) {
        int retryCount = 0;
        boolean deleted = false;
        
        while (!deleted && retryCount < maxRetries) {
            try {
                deleted = deleteDocument(mongoId, collection);
                if (deleted) {
                    return true;
                }
                retryCount++;
                log.error("Delete MongoDB document failed, retry {}/{}: mongoId: {}, collection: {}",
                        retryCount, maxRetries, mongoId, collection);
                
                // 指数退避重试
                Thread.sleep((long) Math.pow(2, retryCount) * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Delete MongoDB document interrupted: {}, mongoId: {}, collection: {}",
                        e.getMessage(), mongoId, collection);
                break;
            } catch (Exception e) {
                retryCount++;
                log.error("Delete MongoDB document failed, retry {}/{}: {}, mongoId: {}, collection: {}",
                        retryCount, maxRetries, e.getMessage(), mongoId, collection);
                
                // 指数退避重试
                try {
                    Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        if (!deleted) {
            log.error("Delete MongoDB document failed after {} retries, adding to cleanup table: mongoId: {}, collection: {}",
                    maxRetries, mongoId, collection);
            // 记录到待清理表
            MongoCleanup cleanup = new MongoCleanup();
            cleanup.setMongoId(mongoId);
            cleanup.setCollection(collection);
            cleanup.setCreateTime(LocalDateTime.now());
            cleanup.setStatus(0); // 待清理
            mongoCleanupMapper.insert(cleanup);
        }
        
        return deleted;
    }
}