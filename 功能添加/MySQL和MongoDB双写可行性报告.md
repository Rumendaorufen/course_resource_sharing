# MySQL和MongoDB数据双写可行性报告

## 1. 项目背景

本项目是一个课程资源共享平台，采用前后端分离架构，基于Spring Boot和Vue 3开发。当前系统主要使用MySQL作为关系型数据库，存储课程、用户、作业等结构化数据，同时使用本地文件系统存储上传的资源文件。系统还实现了基于Caffeine+Redis的多级缓存架构，用于提高系统性能和并发能力。

随着业务发展，系统需要处理大量的非结构化和半结构化数据，如上传的作业资源和资源中心的各类资源。为了提高系统的扩展性、性能和灵活性，考虑引入MongoDB作为辅助数据库，用于存储上传的作业资源和资源中心的资源，并实现MySQL和MongoDB的数据双写。

## 2. 当前存储现状分析

### 2.1 存储架构

当前系统采用MySQL+本地文件存储的架构：

- **MySQL**：存储结构化数据，如用户、课程、作业、资源元数据等
- **本地文件系统**：存储上传的资源文件，文件路径存储在MySQL中

### 2.2 核心实体分析

#### 2.2.1 资源实体（Resource）
- 存储资源的基本信息、文件路径、关联的课程和上传者信息
- 表结构：resource
- 核心字段：id, name, description, filePath, fileName, fileSize, uploaderUserId, courseId, downloadCount, type, status

#### 2.2.2 作业实体（Assignment）
- 存储作业的基本信息、关联的课程和教师信息
- 表结构：assignment
- 核心字段：id, title, description, courseId, teacherId, deadline, status

#### 2.2.3 作业提交实体（HomeworkSubmission）
- 存储学生提交的作业内容和附件信息
- 表结构：homework_submission
- 核心字段：id, assignmentId, studentId, content, attachmentUrl, attachmentName, attachmentSize, status, score, comment

### 2.3 当前存储存在的问题

1. **半结构化数据存储不灵活**：资源和作业数据包含大量半结构化信息，如资源元数据、作业内容等，使用MySQL存储不够灵活
2. **本地文件存储扩展性差**：本地文件系统存储资源文件，不利于分布式部署和横向扩展
3. **查询性能瓶颈**：随着数据量增长，MySQL在处理复杂查询和大量数据时性能会下降
4. **读写分离困难**：对于资源中心的高并发读写场景，MySQL的读写分离配置复杂
5. **数据模型演进困难**：关系型数据库的Schema变更成本高，不适合快速迭代的业务需求

## 3. 引入MongoDB的优势和适合场景

### 3.1 MongoDB的优势

1. **文档存储**：MongoDB采用BSON（类似JSON）格式存储数据，适合存储半结构化和非结构化数据
2. **高可用性**：支持副本集（Replica Set），提供自动故障转移和数据冗余
3. **扩展性**：支持分片（Sharding），可以横向扩展以处理大量数据
4. **高性能**：内存映射存储引擎，读写性能优异，适合高并发场景
5. **灵活的Schema**：无模式设计，允许动态调整数据结构，适合快速迭代的业务需求
6. **强大的查询能力**：支持丰富的查询操作，包括索引、聚合、地理空间查询等
7. **支持二进制数据**：可以直接存储文件数据（GridFS），无需依赖外部文件系统

### 3.2 适合场景

1. **资源元数据存储**：资源中心的资源元数据包含大量半结构化信息，适合存储在MongoDB中
2. **作业资源存储**：学生提交的作业资源（如文档、图片、视频等）适合存储在MongoDB中
3. **高并发读写场景**：资源中心的高频读写操作，MongoDB的性能优势明显
4. **数据模型动态演进**：业务快速迭代时，MongoDB的灵活Schema便于数据模型调整
5. **分布式部署**：支持分片和副本集，适合分布式部署的场景

## 4. MySQL和MongoDB双写方案设计

### 4.1 数据模型设计

#### 4.1.1 MySQL数据模型
- 保持现有的关系型数据模型不变
- 继续存储用户、课程、作业等结构化数据
- 保留资源和作业的核心元数据
- **新增mongoId字段**：用于关联MongoDB文档
  - resource表：添加mongoId字段
  - homework_submission表：添加mongoId字段
  - assignment表：添加mongoId字段

**核心表结构变更**：
```sql
-- 资源表添加mongoId字段
ALTER TABLE resource ADD COLUMN mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID';

-- 作业提交表添加mongoId字段
ALTER TABLE homework_submission ADD COLUMN mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID';

-- 作业表添加mongoId字段
ALTER TABLE assignment ADD COLUMN mongo_id VARCHAR(36) NOT NULL COMMENT 'MongoDB文档ID';
```

#### 4.1.2 MongoDB数据模型

**资源文档（Resource）**：
```json
{
  "_id": "f8b4c9d3-e6a2-4c5d-9f8e-7a6b5c4d3b2a", // 使用UUID作为mongoId
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

**作业提交文档（HomeworkSubmission）**：
```json
{
  "_id": "a1b2c3d4-e5f6-7g8h-9i0j-1k2l3m4n5o6p", // 使用UUID作为mongoId
  "submissionId": 1, // 与MySQL的homework_submission.id对应
  "assignmentId": 1,
  "studentId": 1,
  "studentName": "学生A",
  "content": "作业内容",
  "attachment": {
    "url": "/uploads/2024/05/20/homework1.pdf",
    "name": "homework1.pdf",
    "size": 512000
  },
  "status": "submitted",
  "score": 90,
  "comment": "优秀",
  "feedback": "继续保持",
  "submitTime": ISODate("2024-05-20T14:30:00Z"),
  "gradeTime": ISODate("2024-05-20T16:00:00Z"),
  "createTime": ISODate("2024-05-20T14:30:00Z"),
  "updateTime": ISODate("2024-05-20T16:00:00Z")
}
```

### 4.2 双写实现方案

#### 4.2.1 先写数据库，再写MongoDB（不推荐）

**实现方式**：
1. 在业务服务层，先写入MySQL，再写入MongoDB
2. 使用事务确保双写的原子性
3. 若任一写入失败，执行回滚操作

**代码示例**：
```java
@Transactional
public Resource saveResource(Resource resource) {
    // 1. 写入MySQL
    resourceMapper.insert(resource);
    
    // 2. 转换为MongoDB文档
    ResourceDocument resourceDoc = convertToDocument(resource);
    
    // 3. 写入MongoDB
    resourceRepository.save(resourceDoc);
    
    return resource;
}
```

**优点**：
- 数据一致性强
- 实现简单，易于理解

**缺点**：
- 增加了写入延迟
- 降低了系统吞吐量
- 若MongoDB不可用，会影响MySQL的写入
- 网络异常时可能导致数据不一致

**适用场景**：
- 对数据完整性要求不高的业务场景
- 非核心数据可有可无的场景

#### 4.2.2 先写MongoDB，再写数据库（推荐）

**核心设计思路**：
1. 查询MongoDB文档中的数据，必须通过数据库表中保存的mongoId
2. 若mongoId在数据库中未保存成功，MongoDB中的数据永远不会被查询到
3. 新增数据时：先写MongoDB，再写数据库
4. 修改数据时：新增MongoDB文档，生成新mongoId，再修改数据库
5. 删除MongoDB旧数据时：支持两种实现方式（带消息队列/无消息队列）

**新增数据流程**：
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

### 4.2.2.1 带消息队列的实现

**修改数据流程**：
```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  1. 生成新的   │     │  2. 新增MongoDB │     │  3. 修改       │
│  mongoId      │────▶│  文档（不直接   │────▶│  数据库表，     │
│               │     │  修改旧文档）  │     │  更新mongoId   │
└───────────────┘     └───────────────┘     └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  4. 发送删除   │
                                          │  旧MongoDB文档 │
                                          │  的MQ消息      │
                                          └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  5. 消费MQ     │
                                          │  消息，删除    │
                                          │  旧MongoDB文档 │
                                          └───────────────┘
```

**代码示例**：

1. 新增数据：
```java
@Transactional
public Resource saveResource(Resource resource) {
    // 1. 生成mongoId
    String mongoId = UUID.randomUUID().toString();
    resource.setMongoId(mongoId);
    
    // 2. 写入MongoDB
    ResourceDocument resourceDoc = convertToDocument(resource);
    resourceRepository.save(resourceDoc);
    
    // 3. 写入MySQL
    resourceMapper.insert(resource);
    
    return resource;
}
```

2. 修改数据：
```java
@Transactional
public Resource updateResource(Resource resource) {
    // 1. 生成新的mongoId
    String newMongoId = UUID.randomUUID().toString();
    resource.setMongoId(newMongoId);
    
    // 2. 查询旧数据，获取旧mongoId
    Resource oldResource = resourceMapper.selectById(resource.getId());
    String oldMongoId = oldResource.getMongoId();
    
    // 3. 新增MongoDB文档
    ResourceDocument resourceDoc = convertToDocument(resource);
    resourceRepository.save(resourceDoc);
    
    // 4. 修改数据库，更新mongoId
    resourceMapper.updateById(resource);
    
    // 5. 发送删除旧MongoDB文档的MQ消息
    messageProducer.send(new DeleteMongoMessage(oldMongoId, "resource"));
    
    return resource;
}
```

3. MQ消费者处理删除：
```java
@RabbitListener(queues = "delete_mongo_queue")
public void handleDeleteMongoMessage(DeleteMongoMessage message) {
    try {
        // 删除MongoDB文档
        if ("resource".equals(message.getCollection())) {
            resourceRepository.deleteById(message.getMongoId());
        } else if ("submission".equals(message.getCollection())) {
            submissionRepository.deleteById(message.getMongoId());
        }
    } catch (Exception e) {
        // 异常会被MQ重试机制处理
        log.error("Delete MongoDB document failed: {}", e.getMessage());
        throw e;
    }
}
```

### 4.2.2.2 无消息队列的实现

**核心设计思路**：
1. 不使用消息队列，直接在业务代码中处理旧MongoDB文档的删除
2. 实现本地重试机制，确保删除操作成功
3. 若本地重试失败，记录日志，由定时任务清理

**修改数据流程**：
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

**代码示例**：

1. 新增数据：
```java
@Transactional
public Resource saveResource(Resource resource) {
    // 1. 生成mongoId
    String mongoId = UUID.randomUUID().toString();
    resource.setMongoId(mongoId);
    
    // 2. 写入MongoDB
    ResourceDocument resourceDoc = convertToDocument(resource);
    resourceRepository.save(resourceDoc);
    
    // 3. 写入MySQL
    resourceMapper.insert(resource);
    
    return resource;
}
```

2. 修改数据：
```java
@Transactional
public Resource updateResource(Resource resource) {
    // 1. 生成新的mongoId
    String newMongoId = UUID.randomUUID().toString();
    resource.setMongoId(newMongoId);
    
    // 2. 查询旧数据，获取旧mongoId
    Resource oldResource = resourceMapper.selectById(resource.getId());
    String oldMongoId = oldResource.getMongoId();
    
    // 3. 新增MongoDB文档
    ResourceDocument resourceDoc = convertToDocument(resource);
    resourceRepository.save(resourceDoc);
    
    // 4. 修改数据库，更新mongoId
    resourceMapper.updateById(resource);
    
    // 5. 直接删除旧MongoDB文档（带本地重试）
    deleteOldMongoDocumentWithRetry(oldMongoId, "resource");
    
    return resource;
}

// 带本地重试的MongoDB文档删除方法
private void deleteOldMongoDocumentWithRetry(String mongoId, String collection, int maxRetries) {
    int retryCount = 0;
    boolean deleted = false;
    
    while (!deleted && retryCount < maxRetries) {
        try {
            if ("resource".equals(collection)) {
                resourceRepository.deleteById(mongoId);
            } else if ("submission".equals(collection)) {
                submissionRepository.deleteById(mongoId);
            }
            deleted = true;
        } catch (Exception e) {
            retryCount++;
            log.error("Delete MongoDB document failed, retry {}/{}: {}", 
                     retryCount, maxRetries, e.getMessage());
            
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
        log.error("Delete MongoDB document failed after {} retries, mongoId: {}, collection: {}",
                 maxRetries, mongoId, collection);
        // 记录到待清理表，由定时任务处理
        mongoCleanupMapper.insert(new MongoCleanup(mongoId, collection, LocalDateTime.now()));
    }
}
```

**优点**：
- 数据一致性强
- 无需引入消息队列，降低系统复杂度
- 实现简单，易于理解和维护

**缺点**：
- 增加了业务代码的复杂度
- 删除操作可能阻塞主业务流程
- 本地重试失败时，需要依赖定时任务清理

**适用场景**：
- 系统复杂度要求低
- 消息队列资源受限
- 对数据一致性要求较高，但可以接受短暂延迟

### 4.2.2.3 两种实现方式对比

| 对比维度 | 带消息队列的实现 | 无消息队列的实现 |
|---------|----------------|----------------|
| 系统复杂度 | 高 | 低 |
| 业务代码复杂度 | 低 | 高 |
| 异步处理 | 支持 | 不支持 |
| 重试机制 | 完善（MQ重试+死信队列） | 简单（本地重试+定时任务） |
| 吞吐量 | 高 | 中 |
| 实现成本 | 高（需要部署MQ） | 低 |
| 维护成本 | 高（需要维护MQ） | 低 |

**选择建议**：
- 对于高并发、大数据量的场景，推荐使用带消息队列的实现
- 对于中小规模系统，或消息队列资源受限的场景，推荐使用无消息队列的实现

**无消息队列实现的注意事项**：
1. 合理设置本地重试次数，避免长时间阻塞
2. 实现指数退避策略，避免重试风暴
3. 记录删除失败的日志，便于问题排查
4. 定时任务清理频率根据业务量调整
5. 监控定时任务的执行情况，确保垃圾数据及时清理

**优点**：
- 解决了双写数据一致性问题
- 数据更新操作安全可靠
- 垃圾数据对业务无影响
- 支持两种实现方式，灵活适应不同场景

**缺点**：
- 会产生一定的垃圾数据
- 需要额外的垃圾数据清理机制

#### 4.2.3 垃圾数据清理机制

**1. 定时删除机制**：
- 每天扫描一次MongoDB文档，将mongoId取出到数据库查询
- 只查询时间范围在 [当前时间-25小时, 当前时间-1小时] 的数据
- 若数据库中不存在对应mongoId，删除MongoDB文档
- 数据量大时，可使用多线程处理

**2. 随机删除机制**：
- 每隔500ms随机获取10条数据进行批量处理
- 同样只处理时间范围在 [当前时间-25小时, 当前时间-1小时] 的数据
- 借鉴Redis随机删除策略，避免占用过多CPU资源

**3. MQ重试机制**：
- 使用RocketMQ等支持重试机制的消息队列
- 设置重试次数（如5次），失败后进入死信队列
- 监控死信队列，发现数据则发送告警邮件

**4. 垃圾数据清理流程**：
```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  1. 定时/随机  │     │  2. 遍历MongoDB│     │  3. 数据库中   │
│  获取待清理    │────▶│  文档，提取    │────▶│  不存在对应    │
│  MongoDB文档   │     │  mongoId      │     │  mongoId       │
└───────────────┘     └───────────────┘     └───────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  4. 删除该     │
                                          │  MongoDB文档   │
                                          └───────────────┘
```

### 4.3 一致性保障机制

1. **数据校验和修复**：定期检查MySQL和MongoDB的数据一致性，发现不一致时进行修复
2. **重试机制**：对于异步双写，实现重试机制，确保最终一致性
3. **监控和告警**：实时监控双写状态，发现异常及时告警
4. **版本控制**：为文档添加版本号，确保更新时的数据一致性

## 5. 双写方案对Redis缓存的影响及处理方法

### 5.1 当前Redis缓存架构

当前系统实现了基于Caffeine+Redis的多级缓存架构：

- **一级缓存**：Caffeine本地缓存，存储热点数据
- **二级缓存**：Redis分布式缓存，存储全局数据
- **缓存名称**：userDetails, resourceCache, courseCache, assignmentCache, submissionCache

### 5.2 双写方案对Redis的影响

双写方案主要影响的是resourceCache和submissionCache，因为这两个缓存与资源和作业提交数据相关。

**影响分析**：
1. 双写方案不会直接影响Redis缓存，因为缓存的读写逻辑保持不变
2. 数据更新时，需要确保缓存的一致性
3. 若使用异步双写，可能会出现缓存与MongoDB数据不一致的情况

### 5.3 处理方法

1. **保持现有缓存失效策略**：继续使用@CacheEvict注解在数据更新时清除缓存
2. **统一缓存清除机制**：无论是更新MySQL还是MongoDB，都要确保缓存被正确清除
3. **缓存数据来源选择**：缓存数据优先从MySQL获取，确保缓存与主数据库一致
4. **延迟双删策略**：对于异步双写，可以考虑使用延迟双删策略，确保缓存最终一致

**代码示例**：
```java
@CacheEvict(value = "resourceCache", allEntries = true)
public void updateResource(Resource resource) {
    // 更新MySQL
    resourceMapper.updateById(resource);
    
    // 发送消息到消息队列，异步更新MongoDB
    messageProducer.send(new ResourceMessage(resource.getId(), "UPDATE"));
    
    // 延迟双删，确保异步更新MongoDB后缓存也被清除
    redisTemplate.opsForValue().set("resource_update_flag_" + resource.getId(), "true", 5, TimeUnit.SECONDS);
}
```

## 6. 实现复杂度和风险评估

### 6.1 实现复杂度评估

| 评估维度 | 复杂度 | 说明 |
|---------|--------|------|
| 技术复杂度 | 中等 | 需要掌握MongoDB的使用、Spring Data MongoDB、消息队列等技术 |
| 集成复杂度 | 低 | Spring Boot对MongoDB和消息队列有良好的支持，集成相对简单 |
| 代码修改量 | 中等 | 需要修改资源和作业相关的服务层代码，添加双写逻辑 |
| 测试复杂度 | 高 | 需要测试双写的一致性、性能、容错性等多个方面 |

### 6.2 风险评估

| 风险类型 | 风险等级 | 风险描述 | 缓解措施 |
|---------|--------|----------|----------|
| 数据一致性风险 | 中等 | 异步双写可能导致数据不一致 | 实现数据校验和修复机制，定期检查数据一致性 |
| 性能风险 | 低 | 同步双写会增加写入延迟 | 采用异步双写方案，减少写入延迟 |
| 系统复杂度风险 | 中等 | 引入MongoDB和消息队列会增加系统复杂度 | 完善监控和告警机制，简化部署和运维 |
| 依赖风险 | 低 | 增加了对MongoDB和消息队列的依赖 | 实现服务降级机制，当MongoDB不可用时，系统仍能正常运行 |
| 维护成本风险 | 中等 | 需要维护两套数据库和消息队列 | 完善文档和自动化运维工具，降低维护成本 |

### 6.3 性能影响评估

| 场景 | 性能影响 | 缓解措施 |
|------|----------|----------|
| 写入性能 | 同步双写：降低30%-50%；异步双写：基本无影响 | 采用异步双写方案 |
| 读取性能 | 提高10%-20%，MongoDB的查询性能优于MySQL | 对于复杂查询，优先从MongoDB读取 |
| 系统吞吐量 | 同步双写：降低20%-40%；异步双写：提高10%-20% | 采用异步双写方案，引入消息队列 |

## 7. 结论和建议

### 7.1 可行性结论

基于以上分析，实现MySQL和MongoDB的数据双写是可行的，主要结论如下：

1. **技术可行性**：Spring Boot对MySQL和MongoDB都有良好的支持，实现双写的技术成熟
2. **业务可行性**：引入MongoDB可以解决当前系统存在的存储扩展性、性能和灵活性问题
3. **风险可控**：虽然存在一定的风险，但可以通过合理的设计和实现来缓解
4. **性能提升**：采用异步双写方案，可以提高系统的性能和吞吐量

### 7.2 建议

1. **采用异步双写方案**：优先考虑异步双写方案，减少对主业务流程的影响
2. **引入消息队列**：使用消息队列实现异步双写，如RabbitMQ或Kafka
3. **实现监控和告警**：建立完善的监控和告警机制，及时发现和处理双写异常
4. **逐步迁移数据**：先迁移部分数据到MongoDB，验证双写方案的可行性，再逐步扩大范围
5. **完善文档和测试**：编写详细的设计文档和测试用例，确保双写方案的正确性和可靠性
6. **考虑使用MongoDB GridFS**：对于大文件存储，可以考虑使用MongoDB的GridFS功能，替代本地文件系统

### 7.3 后续工作建议

1. 设计详细的双写实现方案和数据模型
2. 开发和测试双写功能
3. 部署MongoDB和消息队列
4. 逐步迁移数据到MongoDB
5. 监控和优化双写性能
6. 完善运维和监控机制

## 8. 参考文献

1. Spring Boot官方文档
2. MongoDB官方文档
3. Spring Data MongoDB官方文档
4. Redis官方文档
5. 《MongoDB权威指南》
6. 《Spring Boot实战》

---

**报告日期**：2025-12-03
**报告作者**：系统架构组