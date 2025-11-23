# 课程资源共享平台 API 文档

## 简介

本文档详细描述课程资源共享平台后端提供的所有RESTful API接口，包括接口路径、方法、参数、返回值、权限要求等信息。

## 基本信息

- **基础URL**: 所有API的基础路径
- **认证方式**: JWT Token认证，通过Authorization头传递
- **响应格式**: 统一使用`ApiResult<T>`格式返回
- **错误处理**: 错误响应包含错误码和错误信息

## 统一响应格式

所有API接口返回的数据格式统一为：

```json
{
  "code": 200,          // 状态码，200表示成功，其他表示错误
  "message": "操作成功",  // 响应消息
  "data": {}            // 响应数据，根据不同接口返回不同结构
}
```

## 1. 认证管理API

### 1.1 用户登录

**接口路径**: `POST /auth/login`

**功能描述**: 用户登录，获取访问令牌

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN",
      "realName": "管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "classname": null,
      "avatar": null,
      "enabled": true,
      "createTime": "2024-01-01 00:00:00",
      "lastLoginTime": "2024-01-01 00:00:00"
    }
  }
}
```

**权限要求**: 无

### 1.2 用户注册

**接口路径**: `POST /auth/register`

**功能描述**: 新用户注册

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 用户名，长度4-20 |
| password | String | 是 | 密码 |
| role | String | 是 | 角色(STUDENT/TEACHER) |
| realName | String | 是 | 真实姓名 |
| email | String | 否 | 邮箱地址 |
| phone | String | 否 | 电话号码 |
| classname | String | 否 | 班级名称(学生必填) |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 无

### 1.3 获取当前用户信息

**接口路径**: `GET /auth/current-user`

**功能描述**: 获取当前登录用户信息

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "classname": null,
    "avatar": null,
    "enabled": true,
    "createTime": "2024-01-01 00:00:00",
    "lastLoginTime": "2024-01-01 00:00:00"
  }
}
```

**权限要求**: 需要登录

### 1.4 用户登出

**接口路径**: `POST /auth/logout`

**功能描述**: 用户登出

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要登录

## 2. 用户管理API

### 2.1 获取用户详情

**接口路径**: `GET /users/{id}`

**功能描述**: 根据用户ID获取用户详细信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 用户ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "classname": null,
    "avatar": null,
    "enabled": true,
    "createTime": "2024-01-01 00:00:00",
    "lastLoginTime": "2024-01-01 00:00:00"
  }
}
```

**权限要求**: 需要登录

### 2.2 更新用户信息

**接口路径**: `PUT /users/{id}`

**功能描述**: 更新用户信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 用户ID |
| username | String | 是 | 用户名，长度4-20 |
| realName | String | 是 | 真实姓名 |
| email | String | 否 | 邮箱地址 |
| phone | String | 否 | 电话号码 |
| classname | String | 否 | 班级名称(学生必填) |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要登录且只能修改自己的信息，管理员可以修改所有用户信息

### 2.3 更新密码

**接口路径**: `PUT /users/{id}/password`

**功能描述**: 更新用户密码

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 用户ID |
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要登录且只能修改自己的密码

### 2.4 获取所有教师列表

**接口路径**: `GET /users/teachers`

**功能描述**: 获取系统中所有教师用户

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 2,
      "username": "teacher1",
      "role": "TEACHER",
      "realName": "张老师",
      "email": "teacher1@example.com",
      "phone": "13900139001",
      "classname": null,
      "avatar": null,
      "enabled": true,
      "createTime": "2024-01-01 00:00:00",
      "lastLoginTime": "2024-01-01 00:00:00"
    }
  ]
}
```

**权限要求**: 需要登录

### 2.5 获取所有用户

**接口路径**: `GET /users/all`

**功能描述**: 获取系统中所有用户

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "role": "ADMIN",
      "realName": "管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "classname": null,
      "avatar": null,
      "enabled": true,
      "createTime": "2024-01-01 00:00:00",
      "lastLoginTime": "2024-01-01 00:00:00"
    }
  ]
}
```

**权限要求**: 需要ADMIN角色

### 2.6 新增用户

**接口路径**: `POST /users/add`

**功能描述**: 管理员添加新用户

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 用户名，长度4-20 |
| password | String | 是 | 密码 |
| role | String | 是 | 角色(STUDENT/TEACHER) |
| realName | String | 是 | 真实姓名 |
| email | String | 否 | 邮箱地址 |
| phone | String | 否 | 电话号码 |
| classname | String | 否 | 班级名称(学生必填) |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要ADMIN角色

### 2.7 删除用户

**接口路径**: `DELETE /users/{id}`

**功能描述**: 管理员删除用户（不能删除管理员）

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 用户ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要ADMIN角色

## 3. 课程管理API

### 3.1 创建课程

**接口路径**: `POST /courses`

**功能描述**: 管理员创建新课程

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| name | String | 是 | 课程名称 |
| description | String | 否 | 课程描述 |
| teacherId | Long | 是 | 教师ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要ADMIN角色

### 3.2 更新课程信息

**接口路径**: `PUT /courses/{id}`

**功能描述**: 管理员更新课程信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 课程ID |
| name | String | 是 | 课程名称 |
| description | String | 否 | 课程描述 |
| teacherId | Long | 是 | 教师ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要ADMIN角色

### 3.3 删除课程

**接口路径**: `DELETE /courses/{id}`

**功能描述**: 管理员删除课程

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 课程ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要ADMIN角色

### 3.4 获取课程详情

**接口路径**: `GET /courses/{id}`

**功能描述**: 根据ID获取课程详情

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 课程ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "高等数学",
    "description": "这是一门高等数学课程",
    "teacherId": 2,
    "teacherName": "张老师",
    "studentCount": 45,
    "resourceCount": 12,
    "homeworkCount": 8
  }
}
```

**权限要求**: 需要登录

### 3.5 获取课程列表

**接口路径**: `GET /courses`

**功能描述**: 获取所有课程列表

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "高等数学",
      "description": "这是一门高等数学课程",
      "teacherId": 2,
      "teacherName": "张老师",
      "studentCount": 45,
      "resourceCount": 12,
      "homeworkCount": 8
    }
  ]
}
```

**权限要求**: 需要登录

### 3.6 获取所有课程（公开）

**接口路径**: `GET /courses/all`

**功能描述**: 获取系统中所有课程列表，所有用户均可访问

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "高等数学",
      "description": "这是一门高等数学课程",
      "teacherId": 2,
      "teacherName": "张老师"
    }
  ]
}
```

**权限要求**: 无

### 3.7 获取教师课程列表

**接口路径**: `GET /courses/teacher/{teacherId}`

**功能描述**: 获取指定教师ID的课程列表

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| teacherId | Long | 是 | 教师ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "高等数学",
      "description": "这是一门高等数学课程",
      "teacherId": 2,
      "teacherName": "张老师"
    }
  ]
}
```

**权限要求**: 需要登录

### 3.8 获取学生课程列表

**接口路径**: `GET /courses/student/{studentId}`

**功能描述**: 获取指定学生ID的课程列表

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| studentId | Long | 是 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "高等数学",
      "description": "这是一门高等数学课程",
      "teacherId": 2,
      "teacherName": "张老师"
    }
  ]
}
```

**权限要求**: 需要登录

### 3.9 学生选课

**接口路径**: `POST /courses/{id}/select`

**功能描述**: 学生选择课程

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 课程ID |
| studentId | Long | 是 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要STUDENT角色

### 3.10 学生退课

**接口路径**: `POST /courses/{id}/drop`

**功能描述**: 学生退选课程

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 课程ID |
| studentId | Long | 是 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要STUDENT角色

### 3.11 批量添加学生到课程

**接口路径**: `POST /courses/{courseId}/students/class`

**功能描述**: 教师按班级批量添加学生到指定课程

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 是 | 课程ID |
| classname | String | 是 | 班级名称 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要TEACHER角色

### 3.12 添加单个学生到课程

**接口路径**: `POST /courses/{courseId}/students/{studentId}`

**功能描述**: 教师添加单个学生到指定课程

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 是 | 课程ID |
| studentId | Long | 是 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要TEACHER角色

### 3.13 从课程中移除学生

**接口路径**: `DELETE /courses/{courseId}/students/{studentId}`

**功能描述**: 教师从指定课程中移除学生

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 是 | 课程ID |
| studentId | Long | 是 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要TEACHER角色

### 3.14 获取课程中的学生列表

**接口路径**: `GET /courses/{courseId}/students`

**功能描述**: 获取指定课程中的所有学生列表

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 是 | 课程ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 3,
      "username": "student1",
      "role": "STUDENT",
      "realName": "学生1",
      "email": "student1@example.com",
      "phone": "13800138001",
      "classname": "计算机科学1班"
    }
  ]
}
```

**权限要求**: 需要登录

### 3.15 获取不在课程中的学生列表

**接口路径**: `GET /courses/{courseId}/students/not-enrolled`

**功能描述**: 获取未在指定课程中的学生列表，可通过关键词和班级筛选

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 是 | 课程ID |
| keyword | String | 否 | 搜索关键词 |
| classname | String | 否 | 班级名称 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 4,
      "username": "student2",
      "role": "STUDENT",
      "realName": "学生2",
      "email": "student2@example.com",
      "phone": "13800138002",
      "classname": "计算机科学2班"
    }
  ]
}
```

**权限要求**: 需要TEACHER角色

### 3.16 获取所有班级名称

**接口路径**: `GET /courses/classnames`

**功能描述**: 获取系统中所有的班级名称列表

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": ["计算机科学1班", "计算机科学2班", "软件工程1班"]
}
```

**权限要求**: 需要登录

### 3.17 检查学生是否已选课

**接口路径**: `GET /courses/student/enrolled/{courseId}`

**功能描述**: 检查指定学生是否已选择指定课程

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 是 | 课程ID |
| studentId | Long | 是 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**权限要求**: 需要登录

## 4. 资源管理API

### 4.1 获取资源详情

**接口路径**: `GET /resource/{id}`

**功能描述**: 根据ID获取资源详细信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 资源ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "课程大纲",
    "description": "高等数学课程大纲",
    "courseId": 1,
    "uploaderId": 2,
    "type": "document",
    "originalFileName": "大纲.pdf",
    "fileSize": 102400,
    "filePath": "/uploads/1.pdf",
    "downloadCount": 15,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

**权限要求**: 需要登录

### 4.2 上传资源

**接口路径**: `POST /resource`

**功能描述**: 上传新的资源文件

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| file | MultipartFile | 是 | 上传的文件 |
| name | String | 是 | 资源名称 |
| description | String | 否 | 资源描述 |
| courseId | Long | 是 | 课程ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "课程大纲",
    "description": "高等数学课程大纲",
    "courseId": 1,
    "uploaderId": 2,
    "type": "document",
    "originalFileName": "大纲.pdf",
    "fileSize": 102400,
    "filePath": "/uploads/1.pdf",
    "downloadCount": 0,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

**权限要求**: 需要STUDENT、TEACHER或ADMIN角色

### 4.3 下载资源

**接口路径**: `GET /resource/{id}/download`

**功能描述**: 根据资源ID下载资源文件

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 资源ID |

**响应数据**: 文件流

**权限要求**: 需要登录

### 4.4 删除资源

**接口路径**: `DELETE /resource/{id}`

**功能描述**: 删除指定ID的资源。管理员可删除任何资源，其他用户只能删除自己上传的资源

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 资源ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要登录，且只能删除自己上传的资源，或ADMIN角色

### 4.5 获取资源列表

**接口路径**: `GET /resource`

**功能描述**: 获取资源列表，可按课程筛选

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 否 | 课程ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "课程大纲",
      "description": "高等数学课程大纲",
      "courseId": 1,
      "courseName": "高等数学",
      "uploaderId": 2,
      "uploaderName": "张老师",
      "type": "document",
      "originalFileName": "大纲.pdf",
      "fileSize": 102400,
      "downloadCount": 15,
      "createTime": "2024-01-01 00:00:00",
      "updateTime": "2024-01-01 00:00:00"
    }
  ]
}
```

**权限要求**: 需要STUDENT、TEACHER或ADMIN角色

### 4.6 更新资源信息

**接口路径**: `PUT /resource/{id}`

**功能描述**: 更新指定ID的资源信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 资源ID |
| name | String | 是 | 资源名称 |
| description | String | 否 | 资源描述 |
| type | String | 否 | 资源类型 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "更新后的名称",
    "description": "更新后的描述",
    "courseId": 1,
    "uploaderId": 2,
    "type": "document",
    "originalFileName": "大纲.pdf",
    "fileSize": 102400,
    "filePath": "/uploads/1.pdf",
    "downloadCount": 15,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-02 00:00:00"
  }
}
```

**权限要求**: 需要TEACHER角色，且必须是资源上传者

## 5. 作业管理API

### 5.1 发布作业

**接口路径**: `POST /assignments`

**功能描述**: 教师发布新作业

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| title | String | 是 | 作业标题 |
| description | String | 是 | 作业描述 |
| courseId | Long | 是 | 课程ID |
| teacherId | Long | 是 | 教师ID |
| deadline | LocalDateTime | 是 | 截止时间 |
| status | String | 否 | 状态，默认为active |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要TEACHER角色

### 5.2 更新作业

**接口路径**: `PUT /assignments/{id}`

**功能描述**: 更新指定ID的作业信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 作业ID |
| title | String | 是 | 作业标题 |
| description | String | 是 | 作业描述 |
| deadline | LocalDateTime | 是 | 截止时间 |
| status | String | 否 | 状态 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要TEACHER角色，且必须是作业发布者

### 5.3 删除作业

**接口路径**: `DELETE /assignments/{id}`

**功能描述**: 删除指定ID的作业

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 作业ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要TEACHER角色，且必须是作业发布者

### 5.4 获取作业详情

**接口路径**: `GET /assignments/{id}`

**功能描述**: 获取指定ID的作业详细信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 作业ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "第一章作业",
    "description": "完成课本P10-P15的习题",
    "courseId": 1,
    "courseName": "高等数学",
    "teacherId": 2,
    "teacherName": "张老师",
    "deadline": "2024-01-10 23:59:59",
    "status": "active",
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

**权限要求**: 需要登录

### 5.5 获取课程作业列表

**接口路径**: `GET /assignments/course/{courseId}`

**功能描述**: 获取指定课程的所有作业

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| courseId | Long | 是 | 课程ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "第一章作业",
      "description": "完成课本P10-P15的习题",
      "courseId": 1,
      "courseName": "高等数学",
      "teacherId": 2,
      "teacherName": "张老师",
      "deadline": "2024-01-10 23:59:59",
      "status": "active"
    }
  ]
}
```

**权限要求**: 需要登录

### 5.6 获取教师作业列表

**接口路径**: `GET /assignments/teacher/{teacherId}`

**功能描述**: 获取指定教师的所有作业

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| teacherId | Long | 是 | 教师ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "第一章作业",
      "description": "完成课本P10-P15的习题",
      "courseId": 1,
      "courseName": "高等数学",
      "teacherId": 2,
      "teacherName": "张老师",
      "deadline": "2024-01-10 23:59:59",
      "status": "active"
    }
  ]
}
```

**权限要求**: 需要登录

### 5.7 获取作业列表

**接口路径**: `GET /assignments`

**功能描述**: 根据查询参数获取作业列表

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| teacherId | Long | 否 | 教师ID |
| courseId | Long | 否 | 课程ID |
| studentId | Long | 否 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "第一章作业",
      "description": "完成课本P10-P15的习题",
      "courseId": 1,
      "courseName": "高等数学",
      "teacherId": 2,
      "teacherName": "张老师",
      "deadline": "2024-01-10 23:59:59",
      "status": "active"
    }
  ]
}
```

**权限要求**: 需要登录

### 5.8 检查作业是否过期

**接口路径**: `GET /assignments/{id}/deadline-passed`

**功能描述**: 检查指定ID的作业是否过期

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 作业ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": false
}
```

**权限要求**: 需要登录

## 6. 作业提交API

### 6.1 学生提交作业

**接口路径**: `POST /api/homework-submissions/student/{studentId}`

**功能描述**: 学生提交作业

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| studentId | Long | 是 | 学生ID |
| assignmentId | Long | 是 | 作业ID |
| content | String | 否 | 作业内容 |
| attachmentUrl | String | 否 | 附件URL |
| attachmentName | String | 否 | 附件名称 |
| attachmentSize | Long | 否 | 附件大小(字节) |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要STUDENT角色，且只能提交自己的作业

### 6.2 更新作业提交

**接口路径**: `PUT /api/homework-submissions/{id}`

**功能描述**: 更新作业提交内容

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 提交ID |
| content | String | 否 | 作业内容 |
| attachmentUrl | String | 否 | 附件URL |
| attachmentName | String | 否 | 附件名称 |
| attachmentSize | Long | 否 | 附件大小(字节) |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要STUDENT角色，且只能更新自己的提交

### 6.3 删除作业提交

**接口路径**: `DELETE /api/homework-submissions/{id}`

**功能描述**: 删除作业提交记录

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 提交ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要STUDENT或TEACHER角色，学生只能删除自己的提交

### 6.4 批改作业

**接口路径**: `PUT /api/homework-submissions/{id}/grade`

**功能描述**: 教师批改作业并打分

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 提交ID |
| score | Integer | 是 | 分数 |
| comment | String | 否 | 评语 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**权限要求**: 需要TEACHER角色

### 6.5 获取作业提交详情

**接口路径**: `GET /api/homework-submissions/{id}`

**功能描述**: 获取作业提交的详细信息

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 提交ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "assignmentId": 1,
    "assignmentTitle": "第一章作业",
    "studentId": 3,
    "studentName": "学生1",
    "content": "完成了所有习题",
    "attachmentUrl": "/uploads/submission1.pdf",
    "attachmentName": "作业答案.pdf",
    "attachmentSize": 51200,
    "status": "GRADED",
    "score": 95,
    "comment": "完成得很好",
    "submitTime": "2024-01-08 10:00:00",
    "gradeTime": "2024-01-09 14:00:00"
  }
}
```

**权限要求**: 需要STUDENT或TEACHER角色

### 6.6 获取作业的所有提交

**接口路径**: `GET /api/homework-submissions/homework/{homeworkId}`

**功能描述**: 获取指定作业的所有提交记录

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| homeworkId | Long | 是 | 作业ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "assignmentId": 1,
      "assignmentTitle": "第一章作业",
      "studentId": 3,
      "studentName": "学生1",
      "content": "完成了所有习题",
      "status": "GRADED",
      "score": 95,
      "submitTime": "2024-01-08 10:00:00",
      "gradeTime": "2024-01-09 14:00:00"
    }
  ]
}
```

**权限要求**: 需要STUDENT或TEACHER角色

### 6.7 获取学生作业提交历史

**接口路径**: `GET /api/homework-submissions/student/{studentId}/assignment/{assignmentId}/history`

**功能描述**: 获取学生对某个作业的所有提交历史

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| studentId | Long | 是 | 学生ID |
| assignmentId | Long | 是 | 作业ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "assignmentId": 1,
      "studentId": 3,
      "content": "完成了所有习题",
      "submitTime": "2024-01-08 10:00:00",
      "updateTime": "2024-01-08 10:00:00"
    }
  ]
}
```

**权限要求**: 需要STUDENT或TEACHER角色

### 6.8 获取学生最新提交

**接口路径**: `GET /api/homework-submissions/student/{studentId}/assignment/{assignmentId}/latest`

**功能描述**: 获取学生对某个作业的最新提交

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| studentId | Long | 是 | 学生ID |
| assignmentId | Long | 是 | 作业ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "assignmentId": 1,
    "studentId": 3,
    "content": "完成了所有习题",
    "status": "GRADED",
    "score": 95,
    "comment": "完成得很好",
    "submitTime": "2024-01-08 10:00:00",
    "gradeTime": "2024-01-09 14:00:00"
  }
}
```

**权限要求**: 需要STUDENT或TEACHER角色

### 6.9 获取学生所有提交

**接口路径**: `GET /api/homework-submissions/student/{studentId}`

**功能描述**: 获取学生的所有作业提交

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| studentId | Long | 是 | 学生ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "assignmentId": 1,
      "assignmentTitle": "第一章作业",
      "studentId": 3,
      "content": "完成了所有习题",
      "status": "GRADED",
      "score": 95,
      "submitTime": "2024-01-08 10:00:00",
      "gradeTime": "2024-01-09 14:00:00"
    }
  ]
}
```

**权限要求**: 需要STUDENT或TEACHER角色

### 6.10 统计学生在课程中的提交次数

**接口路径**: `GET /api/homework-submissions/count/student/{studentId}/course/{courseId}`

**功能描述**: 统计学生在指定课程中的作业提交次数

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| studentId | Long | 是 | 学生ID |
| courseId | Long | 是 | 课程ID |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 5
}
```

**权限要求**: 需要STUDENT或TEACHER角色

## 7. 仪表盘API

### 7.1 获取统计数据

**接口路径**: `GET /api/dashboard/stats`

**功能描述**: 获取课程、资源、作业的统计数据

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCourses": 10,
    "totalResources": 150,
    "totalAssignments": 80,
    "totalSubmissions": 2400
  }
}
```

**权限要求**: 需要登录

### 7.2 获取最近作业

**接口路径**: `GET /api/dashboard/recent-assignments`

**功能描述**: 获取最近的作业列表

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "第一章作业",
      "courseName": "高等数学",
      "teacherName": "张老师",
      "deadline": "2024-01-10 23:59:59"
    }
  ]
}
```

**权限要求**: 需要登录

### 7.3 获取最新资源

**接口路径**: `GET /api/dashboard/resources/recent`

**功能描述**: 获取最新的资源列表

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "课程大纲",
      "courseName": "高等数学",
      "uploaderName": "张老师",
      "uploadTime": "2024-01-01 00:00:00"
    }
  ]
}
```

**权限要求**: 需要登录

### 7.4 获取月度统计数据

**接口路径**: `GET /api/dashboard/monthly-stats`

**功能描述**: 获取最近6个月的资源和作业统计数据

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "months": ["2023-08", "2023-09", "2023-10", "2023-11", "2023-12", "2024-01"],
    "resourceCounts": [25, 30, 20, 25, 30, 20],
    "assignmentCounts": [15, 10, 12, 10, 15, 8]
  }
}
```

**权限要求**: 需要登录

### 7.5 获取用户总数

**接口路径**: `GET /api/dashboard/users/count`

**功能描述**: 获取系统中的用户总数

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 150
}
```

**权限要求**: 需要登录

## 8. 文件管理API

### 8.1 上传文件

**接口路径**: `POST /file/upload`

**功能描述**: 上传文件（通用文件上传接口）

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| file | MultipartFile | 是 | 上传的文件 |

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "/uploads/20240101/example.pdf"
}
```

**权限要求**: 需要登录

### 8.2 下载文件

**接口路径**: `GET /file/download/{filename:.+}`

**功能描述**: 下载文件

**请求参数**:

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| filename | String | 是 | 文件名 |

**响应数据**: 文件流

**权限要求**: 需要登录

## 9. 密码管理API

### 9.1 加密所有用户密码

**接口路径**: `POST /password/encrypt-all`

**功能描述**: 批量加密所有用户密码（仅用于系统初始化或数据迁移）

**请求参数**: 无

**响应数据**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "所有用户密码已加密"
}
```

**权限要求**: 需要ADMIN角色

## 权限说明

系统采用基于角色的访问控制(RBAC)，主要角色包括：

1. **ADMIN**: 管理员，拥有最高权限，可以管理所有功能
2. **TEACHER**: 教师，可以管理课程、资源、作业，查看和批改学生提交
3. **STUDENT**: 学生，可以查看课程、资源，提交作业

接口权限要求在每个接口文档中已标明。