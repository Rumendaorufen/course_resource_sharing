# MongoDB部署与配置操作文档

## 1. 文档目的

本文档详细描述了在当前课程资源共享平台项目中，为支持MySQL和MongoDB双写方案，需要在MongoDB上执行的操作步骤和注意事项。

## 2. 基础环境准备

### 2.1 部署MongoDB服务
1. **安装MongoDB**：确保安装MongoDB 4.4+版本
2. **启动服务**：配置并启动MongoDB服务
3. **验证服务**：通过`mongo`命令连接测试服务是否正常运行

### 2.2 创建应用数据库
连接到MongoDB后，执行命令创建/切换到应用数据库：
```javascript
use course_resource_sharing
```
> MongoDB中，数据库会在首次插入数据时自动创建，但建议提前切换到目标数据库进行后续操作

### 2.3 创建用户并配置权限
为应用数据库创建具有读写权限的用户：
```javascript
use course_resource_sharing
db.createUser({
  user: "courseApp",  // 应用用户名
  pwd: "coursePass",  // 应用密码
  roles: [
    { role: "readWrite", db: "course_resource_sharing" }  // 读写权限
  ]
})
```

## 3. 数据模型准备

### 3.1 文档结构
根据设计文档，MongoDB中的文档结构应符合以下设计：
- **资源文档**：存储资源元数据，使用UUID作为`_id`
- **作业文档**：存储作业基本信息，使用UUID作为`_id`
- **作业提交文档**：存储学生作业提交信息，使用UUID作为`_id`

### 3.2 集合命名
MongoDB会在首次插入数据时自动创建集合，无需手动创建。集合命名如下：
- 资源集合：`resource`
- 作业集合：`assignment`
- 作业提交集合：`homework_submission`

## 4. 连接验证

### 4.1 测试用户连接
使用创建的用户连接到MongoDB，验证权限：
```bash
mongosh -u courseApp -p coursePass --authenticationDatabase course_resource_sharing
```

### 4.2 测试基本操作
连接成功后，测试基本的CRUD操作：
```javascript
// 测试写入
db.test.insert({ name: "test" })
// 测试读取
db.test.find()
// 删除测试数据
db.test.drop()
```

## 5. 应用集成准备

### 5.1 配置应用连接
在项目的`application.yml`中配置MongoDB连接：
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://courseApp:coursePass@localhost:27017/course_resource_sharing?authSource=course_resource_sharing
      database: course_resource_sharing
```

### 5.2 双写逻辑说明
确保理解设计文档中的双写逻辑：
- 新增数据：先写MongoDB，再写MySQL
- 修改数据：新增MongoDB文档，生成新mongoId，再修改MySQL
- 删除旧MongoDB文档：使用本地重试机制，失败后由定时任务清理

## 6. 监控与维护准备

### 6.1 配置日志
确保MongoDB日志正常记录，便于问题排查

### 6.2 垃圾数据清理机制
设计文档中包含了定时清理垃圾数据的机制，需确保相关配置正确：
- 定时任务每小时执行一次
- 最大重试次数为3次
- 失败的删除操作会记录到`mongo_cleanup`表，由定时任务清理

## 7. 后续操作
1. 等待应用代码开发完成后，部署并测试双写功能
2. 监控双写操作的成功率和性能
3. 定期验证MySQL和MongoDB数据的一致性
4. 根据实际运行情况调整配置（如重试次数、清理频率等）

## 8. 注意事项
- **生产环境安全**：建议启用MongoDB认证和TLS/SSL加密
- **数据备份**：定期备份MongoDB数据
- **监控**：监控MongoDB的连接数、内存使用和磁盘空间
- **最小权限原则**：只给应用用户分配必要的权限
- **版本兼容性**：确保使用的MongoDB驱动与MongoDB服务器版本兼容

## 9. 附录

### 9.1 常用MongoDB命令
| 命令 | 描述 |
|------|------|
| `show dbs` | 显示所有数据库 |
| `use <dbname>` | 切换到指定数据库 |
| `show collections` | 显示当前数据库的所有集合 |
| `db.<collection>.find()` | 查询集合中的所有文档 |
| `db.<collection>.insert(<doc>)` | 插入文档 |
| `db.<collection>.update(<filter>, <update>)` | 更新文档 |
| `db.<collection>.remove(<filter>)` | 删除文档 |
| `db.dropDatabase()` | 删除当前数据库 |
| `db.dropUser(<username>)` | 删除用户 |
| `db.changeUserPassword(<username>, <newPassword>)` | 修改用户密码 |

### 9.2 常见问题排查
1. **连接失败**：检查MongoDB服务是否运行，防火墙是否开放27017端口
2. **认证失败**：检查用户名、密码和认证数据库是否正确
3. **权限不足**：检查用户是否具有所需的角色和权限
4. **性能问题**：检查索引是否合理，查询是否优化

## 10. 文档版本控制
| 版本 | 日期 | 作者 | 描述 |
|------|------|------|------|
| 1.0 | 2025-12-03 | 系统架构组 | 初始版本，描述MongoDB部署与配置操作 |