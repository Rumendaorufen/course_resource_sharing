package com.course.service;

import com.course.document.ResourceDocument;
import com.course.document.AssignmentDocument;
import com.course.document.SubmissionDocument;

/**
 * MongoDB操作服务接口
 */
public interface MongoDbService {
    /**
     * 保存资源到MongoDB
     * @param resourceDoc 资源文档
     * @return 保存后的资源文档
     */
    ResourceDocument saveResource(ResourceDocument resourceDoc);
    
    /**
     * 保存作业到MongoDB
     * @param assignmentDoc 作业文档
     * @return 保存后的作业文档
     */
    AssignmentDocument saveAssignment(AssignmentDocument assignmentDoc);
    
    /**
     * 保存作业提交到MongoDB
     * @param submissionDoc 作业提交文档
     * @return 保存后的作业提交文档
     */
    SubmissionDocument saveSubmission(SubmissionDocument submissionDoc);
    
    /**
     * 删除MongoDB文档
     * @param mongoId 文档ID
     * @param collection 集合名称
     * @return 是否删除成功
     */
    boolean deleteDocument(String mongoId, String collection);
    
    /**
     * 带重试机制的删除MongoDB文档
     * @param mongoId 文档ID
     * @param collection 集合名称
     * @param maxRetries 最大重试次数
     * @return 是否删除成功
     */
    boolean deleteDocumentWithRetry(String mongoId, String collection, int maxRetries);
}