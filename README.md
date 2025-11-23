# 课程资源共享平台

## 项目简介

课程资源共享平台是一个面向高校的现代化在线教学管理系统，旨在为师生提供便捷的课程管理、资源共享和作业互动解决方案。平台采用前后端分离架构，支持多角色权限管理，实现了教学资源的集中化管理和高效共享，有效提升了教学效率和学习体验。

## 主要功能

### 用户管理模块
- 支持管理员、教师、学生三种角色的用户体系
- 完整的用户注册、登录和认证流程
- 基于JWT的安全认证机制
- 细粒度的权限控制和角色管理

### 课程管理模块
- 管理员可创建和管理课程
- 教师可管理课程信息和内容
- 学生可根据权限查看课程详情和参与课程

### 资源管理模块
- 支持多种格式的教学资源上传、下载和分类管理
- 资源权限控制，确保只有授权用户可访问
- 资源搜索和筛选功能，方便快速查找所需资料

### 作业管理模块
- 教师可发布作业、设置截止日期和评分标准
- 学生可在线提交作业文件
- 教师可批改作业并给出详细反馈
- 学生可查看作业成绩和教师评语

### 数据可视化模块
- 提供资源分布、作业完成情况等数据统计图表
- 基于ECharts的数据可视化展示
- 主题切换功能，支持亮色和暗色主题

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

#### 前端架构设计
前端采用典型的Vue 3组件化架构，遵循MVVM设计模式，主要分为以下几个层次：

1. **视图层（Views）**
   - 包含12个主要页面组件，负责用户界面展示
   - 核心视图：Dashboard、Courses、Resources、Assignments、UserManagement等
   - 学生专属视图位于student子目录

2. **组件层（Components）**
   - 通用组件：AdvancedTable、DataCard、NavMenu、ThemeToggle
   - 图表组件：ChartComponent、ResourceDistributionChart以及charts目录下的专用图表组件
   - 支持数据可视化和响应式交互

3. **路由层（Router）**
   - 基于角色的权限路由系统（ADMIN、TEACHER、STUDENT）
   - 支持动态路由加载和导航守卫
   - 错误处理和路由重定向机制

4. **API通信层**
   - 封装的API模块：analytics.js（数据分析）、course.js（课程管理）
   - 统一的Axios请求拦截器和响应处理

5. **状态管理层（Vuex）**
   - 集中管理应用状态
   - 提供状态持久化和响应式更新机制

### 后端架构详情

#### 技术栈
- **核心框架**: Spring Boot 2.7.18
- **安全框架**: Spring Security
- **身份认证**: JWT (JSON Web Token) 0.11.5
- **ORM框架**: MyBatis-Plus 3.5.3.1
- **数据库**: MySQL 8.0.33
- **缓存**: Caffeine 2.9.3
- **API文档**: Knife4j 4.5.0 (基于OpenAPI 3.0)
- **工具库**: Hutool 5.8.20
- **数据库迁移**: Flyway
- **项目构建**: Maven

#### 后端架构设计
后端采用经典的Spring Boot分层架构，严格遵循MVC设计模式和单一职责原则：

1. **控制层（Controller）**
   - 10个专用控制器处理不同业务领域的HTTP请求
   - 主要控制器：AssignmentController、AuthController、CourseController、DashboardController、FileController、ResourceController、UserController等
   - 统一的请求参数验证和响应处理

2. **服务层（Service）**
   - 封装核心业务逻辑
   - 实现事务管理和服务间调用

3. **数据访问层（Mapper）**
   - 基于MyBatis Plus的数据访问接口
   - 支持复杂查询和条件构建

4. **数据模型层（Entity）**
   - 对应数据库表结构的实体类
   - 包含字段映射和关系定义

5. **数据传输对象（DTO/VO）**
   - 用于前后端数据交互的数据结构
   - 支持请求参数封装和响应数据格式化

6. **配置层（Config）**
   - Spring Security安全配置
   - 数据源和MyBatis配置
   - 跨域和其他系统配置

7. **安全层（Security）**
   - JWT认证和授权机制
   - 用户权限验证和访问控制

8. **公共组件（Common/Utils/Exception）**
   - 统一异常处理
   - 工具类和通用组件
   - 常量定义和枚举类型

### 系统架构图和组件交互流程

#### 架构概览
```
+---------------------+                 +---------------------+
|                     |                 |                     |
|     前端应用        |                 |     后端服务        |
|  (Vue 3 + Element)  |◄───────────────►| (Spring Boot + MyBatis) |
|                     |  RESTful API    |                     |
+----------+----------+                 +----------+----------+
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
   - 数据访问层查询数据库
   - 返回处理结果给前端

3. **文件上传下载流程**
   - 前端通过表单或拖放上传文件
   - FileController处理文件上传请求
   - 服务层保存文件并记录元数据
   - 下载时验证权限后返回文件流

4. **数据可视化流程**
   - DashboardController提供统计数据
   - 前端图表组件（ChartComponent等）处理数据
   - 渲染交互式图表展示各类分析结果

系统采用前后端分离架构，通过RESTful API实现松耦合通信，支持水平扩展和独立部署。后端通过Spring Security和JWT保障系统安全，前端通过路由守卫和权限控制确保用户访问权限的正确管理。

## 快速开始

### 环境要求
- **Java**: 1.8 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 14.0 或更高版本
- **MySQL**: 8.0 或更高版本

### 后端部署

1. **克隆项目**:
   ```bash
   git clone <repository_url>
   cd Course_resource_sharing
   ```

2. **配置数据库**:
   - 创建MySQL数据库: `CREATE DATABASE course_sharing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
   - 修改 `backend/src/main/resources/application.yml` 文件，配置数据库连接信息

3. **构建并启动后端服务**:
   ```bash
   cd backend
   mvn clean package
   java -jar target/resource-sharing-1.0-SNAPSHOT.jar
   ```
   或使用IDE直接运行 `CourseResourceSharingApplication.java`

4. **访问API文档**:
   - 服务启动后，API文档地址: [http://localhost:8080/doc.html](http://localhost:8080/doc.html)

### 前端部署

1. **安装依赖**:
   ```bash
   cd frontend
   npm install
   ```

2. **启动开发服务器**:
   ```bash
   npm run serve
   ```

3. **访问应用**:
   - 在浏览器中打开 [http://localhost:3007](http://localhost:3007) (或Vite提供的其他地址)

4. **构建生产版本**:
   ```bash
   npm run build
   ```
   构建产物将生成在 `dist` 目录

## 项目结构

```
Course_resource_sharing/
├── backend/                      # 后端代码 (Spring Boot)
│   ├── src/main/java/com/course/ # Java 源码
│   │   ├── config/               # 配置类
│   │   ├── controller/           # API控制器
│   │   ├── service/              # 业务逻辑层
│   │   ├── mapper/               # 数据访问层
│   │   ├── entity/               # 数据库实体类
│   │   └── ...                   # 其他功能模块
│   ├── src/main/resources/       # 配置文件和静态资源
│   ├── src/test/                 # 测试代码
│   └── pom.xml                   # Maven依赖配置
│
├── frontend/                     # 前端代码 (Vue 3)
│   ├── src/
│   │   ├── api/                  # API请求封装
│   │   ├── assets/               # 静态资源
│   │   │   ├── styles/           # 样式文件
│   │   │   └── themes/           # 主题文件
│   │   ├── components/           # 可复用组件
│   │   │   └── charts/           # 图表组件
│   │   ├── router/               # 路由配置
│   │   ├── store/                # Vuex状态管理
│   │   │   └── modules/          # 模块化状态
│   │   ├── utils/                # 工具函数
│   │   ├── views/                # 页面视图
│   │   ├── App.vue               # 根组件
│   │   └── main.js               # 入口文件
│   ├── index.html                # HTML模板
│   ├── package.json              # npm依赖
│   └── vite.config.js            # Vite配置
│
├── README.md                     # 项目说明文档
└── 修复报告/                     # 项目修复相关文档
```

## 功能亮点

1. **完善的角色权限系统**
   - 基于Spring Security的权限控制
   - 细粒度的接口级权限验证
   - 前端路由守卫确保页面访问安全

2. **现代化UI设计**
   - 基于Element Plus的响应式界面
   - 支持主题切换功能
   - 精心设计的数据可视化图表

3. **高效的数据管理**
   - 基于MyBatis-Plus的ORM框架
   - Caffeine缓存提升系统性能
   - 数据库迁移工具确保数据结构一致性

4. **全面的API文档**
   - 基于Knife4j的OpenAPI 3.0文档
   - 账户：admin
   - 密码：admin
   - 支持在线调试API接口
   - 详细的接口说明和参数文档

## 开发说明

### 前端开发

1. 组件开发规范：遵循Vue 3 Composition API风格
2. 代码规范：使用ESLint和Prettier保持代码质量
3. 状态管理：按功能模块划分Vuex store
4. API请求：统一封装在api目录下

### 后端开发

1. 代码规范：遵循Spring Boot最佳实践
2. 接口设计：RESTful API设计原则
3. 事务管理：使用Spring声明式事务
4. 日志记录：使用SLF4J + Logback


