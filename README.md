# 课程资源共享平台

## 项目简介

课程资源共享平台是一个面向高校的现代化在线教学管理系统，旨在为师生提供便捷的课程管理、资源共享和作业互动解决方案。平台采用前后端分离架构，支持多角色权限管理，实现了教学资源的集中化管理和高效共享，有效提升了教学效率和学习体验。

## 主要功能

### 用户管理模块
- 支持管理员、教师、学生三种角色的用户体系
- 完整的用户注册、登录和认证流程
- 基于JWT的安全认证机制
- 细粒度的权限控制和角色管理
- 用户信息管理和密码重置功能

### 课程管理模块
- 管理员可创建、编辑和管理课程
- 教师可管理课程信息、内容和学生名单
- 学生可根据权限查看课程详情和参与课程
- 课程分类和搜索功能
- 课程资源关联管理

### 资源管理模块
- 支持多种格式的教学资源上传、下载和分类管理
- 资源权限控制，确保只有授权用户可访问
- 资源搜索和筛选功能，方便快速查找所需资料
- 资源预览功能（支持常见格式）
- 资源统计和下载量跟踪

### 作业管理模块
- 教师可发布作业、设置截止日期和评分标准
- 学生可在线提交作业文件
- 教师可批改作业并给出详细反馈
- 学生可查看作业成绩和教师评语
- 作业统计和提交情况分析

### 数据可视化模块
- 提供资源分布、作业完成情况等数据统计图表
- 基于ECharts的数据可视化展示
- 主题切换功能，支持亮色和暗色主题
- 多维度数据统计和分析

## 技术架构

### 前端架构详情

#### 技术栈
- **框架**: Vue 3.3.9（Composition API）
- **路由**: Vue Router 4.2.5
- **状态管理**: Vuex 4.1.0
- **UI组件库**: Element Plus 2.4.3
- **HTTP客户端**: Axios 1.6.2
- **构建工具**: Vite 5.0.4
- **数据可视化**: ECharts 6.0.0
- **表格数据处理**: AdvancedTable组件
- **主题管理**: 自定义主题系统
- **样式预处理器**: SCSS

#### 前端架构设计
前端采用典型的Vue 3组件化架构，遵循MVVM设计模式，主要分为以下几个层次：

1. **视图层（Views）**
   - 包含12个主要页面组件，负责用户界面展示
   - 核心视图：Dashboard、Courses、Resources、Assignments、UserManagement等
   - 学生专属视图位于student子目录
   - 教师专属视图位于teacher子目录

2. **组件层（Components）**
   - 通用组件：AdvancedTable、DataCard、NavMenu、ThemeToggle、UploadComponent
   - 图表组件：ChartComponent、ResourceDistributionChart、AssignmentCompletionChart、CourseStatisticsChart
   - 表单组件：LoginForm、RegisterForm、CourseForm、ResourceForm
   - 支持数据可视化和响应式交互

3. **路由层（Router）**
   - 基于角色的权限路由系统（ADMIN、TEACHER、STUDENT）
   - 支持动态路由加载和导航守卫
   - 错误处理和路由重定向机制
   - 路由懒加载，优化首屏加载速度

4. **API通信层**
   - 封装的API模块：analytics.js、course.js、resource.js、assignment.js、user.js、auth.js
   - 统一的Axios请求拦截器和响应处理
   - 错误处理和重试机制

5. **状态管理层（Vuex）**
   - 集中管理应用状态
   - 按功能模块划分store：user、course、resource、assignment、analytics
   - 提供状态持久化和响应式更新机制
   - 支持异步操作和状态回滚

6. **工具层（Utils）**
   - 通用工具函数：dateFormat、fileSizeFormat、downloadFile
   - 权限管理工具：permission.js
   - 主题管理工具：theme.js

### 后端架构详情

#### 技术栈
- **核心框架**: Spring Boot 2.7.18
- **安全框架**: Spring Security
- **身份认证**: JWT (JSON Web Token) 0.11.5
- **ORM框架**: MyBatis-Plus 3.5.3.1
- **数据库**: MySQL 8.0.33
- **缓存**: Caffeine 2.9.3 + Redis 6.0+（二级缓存架构）
- **API文档**: Knife4j 4.5.0 (基于OpenAPI 3.0)
- **工具库**: Hutool 5.8.20
- **数据库迁移**: Flyway
- **项目构建**: Maven 3.8.1
- **日志框架**: SLF4J + Logback
- **文件存储**: 本地文件系统（支持扩展到云存储）

#### 后端架构设计
后端采用经典的Spring Boot分层架构，严格遵循MVC设计模式和单一职责原则：

1. **控制层（Controller）**
   - 10个专用控制器处理不同业务领域的HTTP请求
   - 主要控制器：
     - AssignmentController：作业管理
     - AuthController：认证管理
     - CourseController：课程管理
     - DashboardController：数据统计
     - FileController：文件上传下载
     - ResourceController：资源管理
     - UserController：用户管理
   - 统一的请求参数验证和响应处理
   - 异常处理和错误码机制

2. **服务层（Service）**
   - 封装核心业务逻辑
   - 实现事务管理和服务间调用
   - 服务接口和实现分离
   - 支持事务嵌套和传播

3. **数据访问层（Mapper）**
   - 基于MyBatis Plus的数据访问接口
   - 支持复杂查询和条件构建
   - 自定义SQL支持
   - 分页查询和排序功能

4. **数据模型层（Entity）**
   - 对应数据库表结构的实体类
   - 包含字段映射和关系定义
   - 支持逻辑删除和乐观锁

5. **数据传输对象（DTO/VO）**
   - 用于前后端数据交互的数据结构
   - 支持请求参数封装和响应数据格式化
   - 数据校验和转换
   - DTO：请求数据传输对象
   - VO：响应数据传输对象

6. **配置层（Config）**
   - Spring Security安全配置
   - 数据源和MyBatis配置
   - 跨域和CORS配置
   - 多级缓存配置（Caffeine + Redis）
   - 日志配置
   - 文件上传配置

7. **安全层（Security）**
   - JWT认证和授权机制
   - 用户权限验证和访问控制
   - 角色基于的访问控制
   - 密码加密和安全存储
   - 防止XSS和CSRF攻击

8. **公共组件（Common/Utils/Exception）**
   - 统一异常处理
   - 工具类和通用组件
   - 常量定义和枚举类型
   - 注解和AOP切面
   - 响应结果封装

### 多级缓存架构

#### 设计目标
- 实现Caffeine（一级缓存）+ Redis（二级缓存）的多级缓存架构
- 保证缓存数据一致性
- 提高系统响应速度和并发能力
- 增强系统容错性
- 防止缓存穿透和缓存击穿

#### 架构组成

##### 一级缓存（Caffeine）
- **位置**：应用进程内
- **特点**：
  - 访问速度快（亚毫秒级）
  - 存储热点数据
  - 容量有限
- **配置**：
  - 最大容量：1000条
  - 过期时间：30分钟
  - 初始容量：100
  - 记录访问统计

##### 二级缓存（Redis）
- **位置**：独立的Redis服务器
- **特点**：
  - 分布式共享
  - 容量大
  - 访问速度较快（毫秒级）
  - 支持持久化
- **配置**：
  - 过期时间：60分钟（比一级缓存长，避免频繁回源）
  - 序列化方式：JSON
  - 连接池配置：最大连接数、超时时间等

#### 缓存查询流程
1. 查询一级缓存（Caffeine）
2. 若未命中，查询二级缓存（Redis）
3. 若未命中，查询数据库
4. 将结果写入一级和二级缓存

#### 缓存更新流程
1. 更新数据库
2. 更新二级缓存（Redis）
3. 更新一级缓存（Caffeine）
4. Redis写入失败时，仅更新一级缓存，保证数据可用性

#### 容错机制
- Redis不可用时，自动降级到Caffeine缓存
- 记录Redis操作日志，便于监控和排查问题
- 空值缓存机制，防止缓存穿透

### 系统架构图和组件交互流程

#### 架构概览
```
+---------------------+                 +---------------------+                 +---------------------+
|                     |                 |                     |                 |                     |
|     前端应用        |                 |     后端服务        |                 |    Redis缓存        |
|  (Vue 3 + Element)  |◄───────────────►| (Spring Boot + MyBatis) |◄───────────────►|                     |
|                     |  RESTful API    |                     |  缓存操作       |                     |
+----------+----------+                 +----------+----------+                 +---------------------+
           |                                        |
           |                                        |
           ▼                                        ▼
+----------+----------+                 +----------+----------+
|                     |                 |                     |
|  浏览器存储         |                 |    MySQL 数据库     |
| (LocalStorage/Cookie) |                 |                     |
|                     |                 |                     |
+---------------------+                 +---------------------+
```

#### 主要交互流程

1. **用户认证流程**
   - 用户通过Login.vue提交凭据
   - AuthController验证用户信息
   - 服务层生成JWT Token返回
   - 前端存储Token并设置路由权限

2. **资源访问流程**
   - 前端组件发起API请求
   - 请求拦截器添加JWT Token
   - 后端验证Token和权限
   - 服务层处理业务逻辑
   - 先查询多级缓存（Caffeine → Redis）
   - 若缓存未命中，查询数据库
   - 将结果写入多级缓存
   - 返回处理结果给前端

3. **文件上传下载流程**
   - 前端通过表单或拖放上传文件
   - FileController处理文件上传请求
   - 服务层保存文件并记录元数据
   - 更新相关缓存
   - 下载时验证权限后返回文件流

4. **数据可视化流程**
   - DashboardController提供统计数据
   - 前端图表组件（ChartComponent等）处理数据
   - 渲染交互式图表展示各类分析结果

## 快速开始

### 环境要求
- **Java**: 1.8 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 14.0 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本（可选，用于二级缓存）

### 后端部署

1. **克隆项目**:
   ```bash
   git clone <repository_url>
   cd Course_resource_sharing
   ```

2. **配置数据库**:
   - 创建MySQL数据库: `CREATE DATABASE course_sharing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
   - 修改 `backend/src/main/resources/application.yml` 文件，配置数据库连接信息

3. **配置Redis（可选）**:
   - 确保Redis服务已启动
   - 修改 `backend/src/main/resources/application.yml` 文件，配置Redis连接信息

4. **构建并启动后端服务**:
   ```bash
   cd backend
   mvn clean package -DskipTests
   java -jar target/resource-sharing-1.0-SNAPSHOT.jar
   ```
   或使用IDE直接运行 `CourseResourceSharingApplication.java`

5. **访问API文档**:
   - 服务启动后，API文档地址: [http://localhost:8020/api/doc.html](http://localhost:8020/api/doc.html)
   - 账户：admin
   - 密码：admin

### 前端部署

1. **安装依赖**:
   ```bash
   cd frontend
   npm install
   ```

2. **配置API地址**:
   - 修改 `frontend/src/utils/request.js` 文件中的API地址配置

3. **启动开发服务器**:
   ```bash
   npm run serve
   ```

4. **访问应用**:
   - 在浏览器中打开 [http://localhost:3001](http://localhost:3001) (或Vite提供的其他地址)

5. **构建生产版本**:
   ```bash
   npm run build
   ```
   构建产物将生成在 `dist` 目录

## 项目结构

```
Course_resource_sharing/
├── backend/                      # 后端代码 (Spring Boot)
│   ├── db/                       # 数据库迁移脚本
│   ├── logs/                     # 日志文件
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/course/  # Java 源码
│   │   │   │   ├── common/       # 公共组件
│   │   │   │   │   ├── annotation/ # 自定义注解
│   │   │   │   │   ├── api/       # API响应封装
│   │   │   │   │   ├── aspect/    # AOP切面
│   │   │   │   │   ├── constant/  # 常量定义
│   │   │   │   │   └── exception/ # 异常处理
│   │   │   │   ├── config/        # 配置类
│   │   │   │   │   ├── CacheConfig.java # 多级缓存配置
│   │   │   │   │   ├── SecurityConfig.java # 安全配置
│   │   │   │   │   └── WebMvcConfig.java # Web配置
│   │   │   │   ├── controller/    # API控制器
│   │   │   │   ├── dto/           # 请求数据传输对象
│   │   │   │   ├── entity/        # 数据库实体类
│   │   │   │   ├── enums/         # 枚举类型
│   │   │   │   ├── exception/     # 异常定义
│   │   │   │   ├── mapper/        # 数据访问层
│   │   │   │   ├── security/      # 安全相关
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   │   └── impl/      # 业务逻辑实现
│   │   │   │   ├── utils/         # 工具类
│   │   │   │   └── vo/            # 响应数据传输对象
│   │   │   └── resources/         # 配置文件和静态资源
│   │   │       ├── application.yml # 主配置文件
│   │   │       └── logback-spring.xml # 日志配置
│   │   └── test/                  # 测试代码
│   └── pom.xml                    # Maven依赖配置
│
├── frontend/                     # 前端代码 (Vue 3)
│   ├── public/                   # 公共静态资源
│   ├── src/
│   │   ├── api/                  # API请求封装
│   │   ├── assets/               # 静态资源
│   │   │   ├── images/           # 图片资源
│   │   │   ├── styles/           # 样式文件
│   │   │   └── themes/           # 主题文件
│   │   ├── components/           # 可复用组件
│   │   │   ├── charts/           # 图表组件
│   │   │   └── common/           # 通用组件
│   │   ├── router/               # 路由配置
│   │   ├── store/                # Vuex状态管理
│   │   │   └── modules/          # 模块化状态
│   │   ├── utils/                # 工具函数
│   │   ├── views/                # 页面视图
│   │   │   ├── admin/            # 管理员视图
│   │   │   ├── teacher/          # 教师视图
│   │   │   └── student/          # 学生视图
│   │   ├── App.vue               # 根组件
│   │   └── main.js               # 入口文件
│   ├── .env                      # 环境变量配置
│   ├── index.html                # HTML模板
│   ├── package.json              # npm依赖
│   └── vite.config.js            # Vite配置
│
├── logs/                         # 项目日志
├── uploads/                      # 文件上传目录
├── temp/                         # 临时文件目录
├── 修复报告/                     # 项目修复相关文档
├── 功能添加/                     # 功能添加相关文档
│   ├── 多级缓存架构设计文档.md     # 多级缓存架构设计文档
│   └── 使用 Caffeine 和 Redis 实现高效的二级缓存架构.md
└── README.md                     # 项目说明文档
```

## 功能亮点

1. **完善的角色权限系统**
   - 基于Spring Security的权限控制
   - 细粒度的接口级权限验证
   - 前端路由守卫确保页面访问安全
   - 动态权限分配和管理

2. **现代化UI设计**
   - 基于Element Plus的响应式界面
   - 支持主题切换功能（亮色/暗色）
   - 精心设计的数据可视化图表
   - 移动端适配

3. **高效的数据管理**
   - 基于MyBatis-Plus的ORM框架
   - 多级缓存架构（Caffeine + Redis）提升系统性能
   - 数据库迁移工具确保数据结构一致性
   - 支持批量操作和事务管理

4. **全面的API文档**
   - 基于Knife4j的OpenAPI 3.0文档
   - 支持在线调试API接口
   - 详细的接口说明和参数文档
   - 支持API版本管理

5. **可靠的文件管理**
   - 支持多种文件格式上传和下载
   - 文件大小限制和类型验证
   - 文件存储路径配置
   - 支持文件预览功能

6. **强大的数据可视化**
   - 基于ECharts的丰富图表类型
   - 多维度数据统计和分析
   - 实时数据更新
   - 支持图表导出功能

7. **高可用性和容错设计**
   - Redis不可用时自动降级到本地缓存
   - 异常捕获和统一处理
   - 详细的日志记录
   - 支持服务监控和健康检查

## 开发说明

### 前端开发

1. **开发环境搭建**
   - 安装Node.js 14.0+和npm
   - 安装依赖：`npm install`
   - 启动开发服务器：`npm run serve`

2. **组件开发规范**
   - 遵循Vue 3 Composition API风格
   - 使用TypeScript进行类型检查（可选）
   - 组件命名采用 PascalCase 规范
   - 组件文件命名采用 kebab-case 规范

3. **代码规范**
   - 使用ESLint和Prettier保持代码质量
   - 遵循JavaScript Standard Style或Airbnb JavaScript Style
   - 代码注释规范

4. **状态管理**
   - 按功能模块划分Vuex store
   - 使用 mutations 进行同步状态更新
   - 使用 actions 进行异步操作
   - 状态命名采用 camelCase 规范

5. **API请求**
   - 统一封装在api目录下
   - 使用Axios拦截器处理请求和响应
   - 错误处理和重试机制
   - API版本管理

### 后端开发

1. **开发环境搭建**
   - 安装Java 1.8+和Maven
   - 安装MySQL 8.0+和Redis 6.0+（可选）
   - 配置IDE（IntelliJ IDEA或Eclipse）

2. **代码规范**
   - 遵循Spring Boot最佳实践
   - 类命名采用 PascalCase 规范
   - 方法和变量命名采用 camelCase 规范
   - 代码注释规范
   - 使用Lombok简化代码

3. **接口设计**
   - RESTful API设计原则
   - HTTP方法使用规范
   - 状态码使用规范
   - 请求和响应格式统一

4. **事务管理**
   - 使用Spring声明式事务
   - 事务传播行为配置
   - 事务回滚规则

5. **日志记录**
   - 使用SLF4J + Logback
   - 日志级别规范
   - 日志格式统一
   - 关键操作日志记录

6. **测试**
   - 单元测试：使用JUnit和Mockito
   - 集成测试：使用Spring Boot Test
   - API测试：使用Postman或Swagger

## API文档

### 访问地址
- 本地环境：[http://localhost:8020/api/doc.html](http://localhost:8020/api/doc.html)
- 账户：admin
- 密码：admin

### 文档功能
- 在线API文档浏览
- 接口参数和响应示例
- 在线调试API接口
- 支持导出API文档
- 接口分组和版本管理

## 部署说明

### 开发环境部署
- 按照快速开始指南配置和启动服务

### 生产环境部署

1. **后端部署**
   - 构建生产版本：`mvn clean package -DskipTests`
   - 部署jar包到服务器
   - 配置环境变量和启动脚本
   - 使用Nginx作为反向代理

2. **前端部署**
   - 构建生产版本：`npm run build`
   - 部署dist目录到Nginx服务器
   - 配置Nginx反向代理和静态资源访问

3. **数据库部署**
   - 安装MySQL 8.0+数据库
   - 导入初始化数据
   - 配置主从复制和备份策略

4. **Redis部署**
   - 安装Redis 5.0+服务器
   - 配置Redis集群（可选）
   - 配置持久化策略
   - 配置密码和访问控制

### 容器化部署（可选）
- 支持Docker和Docker Compose部署
- 提供Dockerfile和docker-compose.yml文件
- 一键部署所有服务

## 监控与维护

### 日志监控
- 后端日志存储在 `backend/logs` 目录
- 支持日志分级和滚动
- 可配置日志输出到文件或ELK等日志系统

### 应用监控
- 支持Spring Boot Actuator
- 健康检查端点：`/actuator/health`
- 指标端点：`/actuator/metrics`
- 可集成Prometheus和Grafana进行监控

### 缓存监控
- Caffeine缓存统计
- Redis监控（使用Redis CLI或监控工具）
- 缓存命中率统计

### 维护建议
1. 定期清理过期缓存和日志文件
2. 监控数据库连接池和Redis连接数
3. 定期备份数据库和重要文件
4. 监控系统性能指标（CPU、内存、磁盘、网络）
5. 定期更新依赖库和安全补丁


**课程资源共享平台** - 让教学更高效，学习更便捷！