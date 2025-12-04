# MySQL和MongoDB双写设计文档（无消息队列方案）

## 1. 方案概述

### 1.1 方案名称
MySQL和MongoDB数据双写实现（无消息队列方案）

### 1.2 方案目标
- 实现MySQL和MongoDB的数据双写
- 确保数据一致性
- 无需引入消息队列，降低系统复杂度
- 支持资源、作业和作业提交数据的双写

### 1.3 适用场景
- 系统复杂度要求低
- 消息队列资源受限
- 中小规模系统
- 对数据一致性要求较高，但可以接受短暂延迟

### 1.4 核心设计思路
1. 查询MongoDB文档中的数据，必须通过数据库表中保存的mongoId
2. 若mongoId在数据库中未保存成功，MongoDB中的数据永远不会被查询到
3. 新增数据时：先写MongoDB，再写数据库
4. 修改数据时：新增MongoDB文档，生成新mongoId，再修改数据库
5. 删除旧MongoDB文档时：直接在业务代码中处理，实现本地重试机制
6. 若本地重试失败，记录日志，由定时任务清理

## 2. 数据模型设计

### 2.1 MySQL数据模型

#### 2.1.1 资源表（resource）
- **新增字段**：mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID'

**表结构**：
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 资源ID |
| name | VARCHAR(255) | NOT NULL | 资源名称 |
| description | TEXT | | 资源描述 |
| file_path | VARCHAR(255) | NOT NULL | 文件路径 |
| file_name | VARCHAR(255) | NOT NULL | 文件名 |
| file_size | BIGINT | NOT NULL | 文件大小 |
| uploader_user_id | BIGINT | NOT NULL | 上传者ID |
| course_id | BIGINT | NOT NULL | 课程ID |
| download_count | INT | DEFAULT 0 | 下载次数 |
| type | VARCHAR(50) | NOT NULL | 资源类型 |
| status | INT | NOT NULL | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| mongo_id | VARCHAR(36) | NOT NULL | MongoDB文档ID |

#### 2.1.2 作业表（assignment）
- **新增字段**：mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID'

**表结构**：
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 作业ID |
| title | VARCHAR(255) | NOT NULL | 作业标题 |
| description | TEXT | | 作业描述 |
| content | TEXT | | 作业内容 |
| course_id | BIGINT | NOT NULL | 课程ID |
| teacher_id | BIGINT | NOT NULL | 教师ID |
| deadline | DATETIME | NOT NULL | 截止时间 |
| status | INT | NOT NULL | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| mongo_id | VARCHAR(36) | NOT NULL | MongoDB文档ID |

#### 2.1.3 作业提交表（homework_submission）
- **新增字段**：mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID'

**表结构**：
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 提交ID |
| assignment_id | BIGINT | NOT NULL | 作业ID |
| student_id | BIGINT | NOT NULL | 学生ID |
| content | TEXT | | 作业内容 |
| attachment_url | VARCHAR(255) | | 附件URL |
| attachment_name | VARCHAR(255) | | 附件名称 |
| attachment_size | BIGINT | | 附件大小 |
| status | VARCHAR(20) | NOT NULL | 状态 |
| score | INT | | 分数 |
| comment | TEXT | | 评语 |
| feedback | TEXT | | 反馈 |
| submit_time | DATETIME | | 提交时间 |
| grade_time | DATETIME | | 批改时间 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |
| mongo_id | VARCHAR(36) | NOT NULL | MongoDB文档ID |

#### 2.1.4 MongoDB清理表（mongo_cleanup）
- **新增表**：用于记录需要清理的MongoDB文档

**表结构**：
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 清理ID |
| mongo_id | VARCHAR(36) | NOT NULL | MongoDB文档ID |
| collection | VARCHAR(50) | NOT NULL | 集合名称 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| status | INT | NOT NULL DEFAULT 0 | 状态：0-待清理，1-已清理 |
| cleanup_time | DATETIME | | 清理时间 |

### 2.2 MongoDB数据模型

#### 2.2.1 资源集合（resource）
```json
{
  "_id": "f8b4c9d3-e6a2-4c5d-9f8e-7a6b5c4d3b2a", // UUID，与MySQL的mongo_id对应
  "resourceId": 1, // 与MySQL的resource.id对应
  "name": "课程资源",
  "description": "这是一个课程资源",
  "filePath": "/uploads/2024/05/20/file1.pdf",
  "fileName": "file1.pdf",
  "fileSize": 1024000,
  "uploaderUserId": 1,
  "uploaderName": "教师A",
  "courseId": 1,
  "courseName": "Java编程",
  "downloadCount": 10,
  "type": "pdf",
  "status": 1,
  "createTime": ISODate("2024-05-20T10:00:00Z"),
  "updateTime": ISODate("2024-05-20T10:00:00Z")
}
```

#### 2.2.2 作业集合（assignment）
```json
{
  "_id": "a1b2c3d4-e5f6-7g8h-9i0j-1k2l3m4n5o6p", // UUID，与MySQL的mongo_id对应
  "assignmentId": 1, // 与MySQL的assignment.id对应
  "title": "第一次作业",
  "description": "完成教材第1-2章练习题",
  "content": "详细作业要求...",
  "courseId": 1,
  "courseName": "Java编程",
  "teacherId": 1,
  "teacherName": "教师A",
  "deadline": ISODate("2024-05-30T23:59:59Z"),
  "status": 1,
  "createTime": ISODate("2024-05-20T10:00:00Z"),
  "updateTime": ISODate("2024-05-20T10:00:00Z")
}
```

#### 2.2.3 作业提交集合（homework_submission）
```json
{
  "_id": "b2c3d4e5-f6g7-8h9i-0j1k-2l3m4n5o6p7q", // UUID，与MySQL的mongo_id对应
  "submissionId": 1, // 与MySQL的homework_submission.id对应
  "assignmentId": 1,
  "studentId": 1,
  "studentName": "学生A",
  "content": "作业答案...",
  "attachment": {
    "url": "/uploads/2024/05/28/answer.pdf",
    "name": "answer.pdf",
    "size": 512000
  },
  "status": "submitted",
  "score": 90,
  "comment": "优秀",
  "feedback": "继续保持",
  "submitTime": ISODate("2024-05-28T14:30:00Z"),
  "gradeTime": ISODate("2024-05-29T16:00:00Z"),
  "createTime": ISODate("2024-05-28T14:30:00Z"),
  "updateTime": ISODate("2024-05-29T16:00:00Z")
}
```

## 3. 核心流程设计

### 3.1 新增资源流程

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  1. 生成新的   │     │  2. 写入       │     │  3. 写入       │
│  mongoId      │────▶│  MongoDB文档   │────▶│  数据库表      │
└───────────────┘     └───────────────┘     └───────────────┘
         │                                          │
         │                                          ▼
         │                                 ┌───────────────┐
         │                                 │  4. 更新成功   │
         │                                 │  返回结果      │
         │                                 └───────────────┘
         ▼
┌───────────────┐
│  5. 数据库写入 │
│  失败，MongoDB │
│  数据成为垃圾  │
└───────────────┘
```

### 3.2 修改资源流程

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  1. 生成新的   │     │  2. 新增MongoDB │     │  3. 修改       │
│  mongoId      │────▶│  文档（不直接   │────▶│  数据库表，     │
│               │     │  修改旧文档）  │     │  更新mongoId   │
└───────────────┘     └───────────────┘     └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  4. 直接删除   │
                                          │  旧MongoDB文档 │
                                          │  （带本地重试） │
                                          └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  5. 删除成功   │
                                          │  返回结果      │
                                          └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  6. 删除失败   │
                                          │  记录日志，由   │
                                          │  定时任务清理   │
                                          └───────────────┘
```

### 3.3 定时清理垃圾数据流程

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  1. 定时任务   │     │  2. 查询待清理 │     │  3. 遍历待     │
│  触发         │────▶│  记录         │────▶│  清理记录      │
└───────────────┘     └───────────────┘     └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  4. 删除MongoDB│
                                          │  文档         │
                                          └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  5. 更新清理   │
                                          │  记录状态      │
                                          └───────────────┘
```

## 4. 代码实现设计

### 4.1 核心类设计

#### 4.1.1 MongoDbService 接口
```java
public interface MongoDbService {
    /**
     * 保存资源到MongoDB
     */
    ResourceDocument saveResource(ResourceDocument resourceDoc);
    
    /**
     * 保存作业到MongoDB
     */
    AssignmentDocument saveAssignment(AssignmentDocument assignmentDoc);
    
    /**
     * 保存作业提交到MongoDB
     */
    SubmissionDocument saveSubmission(SubmissionDocument submissionDoc);
    
    /**
     * 删除MongoDB文档
     */
    boolean deleteDocument(String mongoId, String collection);
    
    /**
     * 带重试机制的删除MongoDB文档
     */
    boolean deleteDocumentWithRetry(String mongoId, String collection, int maxRetries);
}
```

#### 4.1.2 MongoDbServiceImpl 实现类
```java
@Service
public class MongoDbServiceImpl implements MongoDbService {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private MongoCleanupMapper mongoCleanupMapper;
    
    private static final Logger log = LoggerFactory.getLogger(MongoDbServiceImpl.class);
    
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
            DeleteResult result = mongoTemplate.remove(query, collection);
            return result.getDeletedCount() > 0;
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
```

#### 4.1.3 ResourceServiceImpl 修改
```java
@Service
public class ResourceServiceImpl implements ResourceService {
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private MongoDbService mongoDbService;
    
    @Override
    @Transactional
    public Resource saveResource(Resource resource) {
        // 1. 生成mongoId
        String mongoId = UUID.randomUUID().toString();
        resource.setMongoId(mongoId);
        
        // 2. 转换为MongoDB文档
        ResourceDocument resourceDoc = convertToDocument(resource);
        
        // 3. 写入MongoDB
        mongoDbService.saveResource(resourceDoc);
        
        // 4. 写入MySQL
        resourceMapper.insert(resource);
        
        return resource;
    }
    
    @Override
    @Transactional
    public Resource updateResource(Resource resource) {
        // 1. 生成新的mongoId
        String newMongoId = UUID.randomUUID().toString();
        resource.setMongoId(newMongoId);
        
        // 2. 查询旧数据，获取旧mongoId
        Resource oldResource = resourceMapper.selectById(resource.getId());
        String oldMongoId = oldResource.getMongoId();
        
        // 3. 转换为MongoDB文档
        ResourceDocument resourceDoc = convertToDocument(resource);
        
        // 4. 写入MongoDB
        mongoDbService.saveResource(resourceDoc);
        
        // 5. 更新MySQL
        resourceMapper.updateById(resource);
        
        // 6. 删除旧MongoDB文档，带重试
        mongoDbService.deleteDocumentWithRetry(oldMongoId, "resource", 3);
        
        return resource;
    }
    
    /**
     * 转换Resource实体为MongoDB文档
     */
    private ResourceDocument convertToDocument(Resource resource) {
        // 转换逻辑...
    }
}
```

#### 4.1.4 MongoCleanupJob 定时任务
```java
@Component
public class MongoCleanupJob {
    
    @Autowired
    private MongoCleanupMapper mongoCleanupMapper;
    
    @Autowired
    private MongoDbService mongoDbService;
    
    private static final Logger log = LoggerFactory.getLogger(MongoCleanupJob.class);
    
    /**
     * 每小时执行一次，清理MongoDB垃圾数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupMongoData() {
        log.info("Starting MongoDB cleanup job...");
        
        // 1. 查询待清理记录
        List<MongoCleanup> cleanupList = mongoCleanupMapper.selectByStatus(0);
        
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
```

### 4.2 数据库脚本

#### 4.2.1 添加mongo_id字段
```sql
-- 资源表添加mongoId字段
ALTER TABLE resource ADD COLUMN mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID';

-- 作业表添加mongoId字段
ALTER TABLE assignment ADD COLUMN mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID';

-- 作业提交表添加mongoId字段
ALTER TABLE homework_submission ADD COLUMN mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID';
```

#### 4.2.2 创建mongo_cleanup表
```sql
-- 创建MongoDB清理记录表
CREATE TABLE mongo_cleanup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '清理ID',
    mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID',
    collection VARCHAR(50) NOT NULL COMMENT '集合名称',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    status INT NOT NULL DEFAULT 0 COMMENT '状态：0-待清理，1-已清理',
    cleanup_time DATETIME COMMENT '清理时间',
    INDEX idx_status (status)
) COMMENT 'MongoDB清理记录表';
```

### 4.3 配置文件

#### 4.3.1 application.yml
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/course_resource_sharing
      database: course_resource_sharing
  
# 定时任务配置
course:
  mongo:
    cleanup:
      cron: "0 0 * * * ?"  # 每小时执行一次
      max-retries: 3       # 最大重试次数
```

## 5. 部署和测试方案

### 5.1 部署方案

1. **MongoDB部署**：
   - 安装MongoDB 4.4+版本
   - 配置副本集（可选，用于高可用性）
   - 创建数据库和用户

2. **应用部署**：
   - 更新数据库表结构
   - 部署修改后的应用
   - 启动定时任务

### 5.2 测试方案

#### 5.2.1 单元测试
- 测试MongoDbService的save和delete方法
- 测试带重试机制的delete方法
- 测试转换方法

#### 5.2.2 集成测试
- 测试资源的新增和修改流程
- 测试作业的新增和修改流程
- 测试作业提交的新增和修改流程
- 测试定时清理功能

#### 5.2.3 性能测试
- 测试双写方案的写入性能
- 测试双写方案的读取性能
- 测试高并发场景下的表现

## 6. 风险和注意事项

### 6.1 风险

1. **数据一致性风险**：
   - 数据库写入失败时，MongoDB中的数据成为垃圾数据
   - 删除旧MongoDB文档失败时，可能导致数据冗余

2. **性能风险**：
   - 双写会增加写入延迟
   - 本地重试会阻塞主业务流程

3. **维护风险**：
   - 代码复杂度增加
   - 需要维护两套数据库

### 6.2 注意事项

1. **合理设置重试次数**：
   - 建议设置3-5次重试
   - 采用指数退避策略

2. **监控和告警**：
   - 监控MongoDB的连接状态
   - 监控双写成功率
   - 监控定时任务的执行情况

3. **垃圾数据清理**：
   - 定期检查待清理记录表
   - 监控清理任务的执行情况

4. **数据备份**：
   - 定期备份MongoDB数据
   - 制定数据恢复方案

## 7. 后续优化建议

1. **引入缓存机制**：
   - 缓存MongoDB查询结果
   - 减少数据库访问次数

2. **优化定时任务**：
   - 采用分片处理，提高清理效率
   - 动态调整清理频率

3. **引入监控系统**：
   - 实时监控双写状态
   - 生成性能报告

4. **考虑使用MongoDB GridFS**：
   - 替代本地文件系统，统一存储管理
   - 提高文件存储的扩展性

## 8. 文档版本控制

| 版本 | 作者 | 日期 | 描述 |
|------|------|------|------|
| 1.0 | 系统架构组 | 2025-12-03 | 初始版本，设计无消息队列的双写方案 |

## 9. 审批记录

| 审批人 | 职位 | 日期 | 审批意见 |
|--------|------|------|----------|
| XXX | 技术总监 | 2025-12-03 | 同意实施 |
| XXX | 项目经理 | 2025-12-03 | 同意实施 |

---

**设计文档编写完成**

**编写日期**：2025-12-03
**编写人**：系统架构组