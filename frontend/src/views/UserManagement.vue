<template>
  <div class="user-management">
    <h2>用户管理</h2>
    
    <!-- 添加用户按钮 -->
    <el-button type="primary" @click="showAddDialog" class="add-user-btn">
      <el-icon><Plus /></el-icon>
      添加用户
    </el-button>
    
    <!-- 搜索功能 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="搜索">
        <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/邮箱" />
      </el-form-item>
      <el-form-item>
        <el-select v-model="searchForm.role" placeholder="角色">
          <el-option value="" label="全部" />
          <el-option value="TEACHER" label="教师" />
          <el-option value="STUDENT" label="学生" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 用户列表 -->
    <el-table v-loading="loading" :data="filteredUsers" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.role === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else-if="scope.row.role === 'TEACHER'" type="primary">教师</el-tag>
          <el-tag v-else type="info">学生</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="enabled" label="状态" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.enabled" type="success">启用</el-tag>
          <el-tag v-else type="warning">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDeleteUser(scope.row)"
            :loading="deletingId === scope.row.id"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加用户"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option value="TEACHER" label="教师" />
            <el-option value="STUDENT" label="学生" />
          </el-select>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddUser" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../utils/request'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const deletingId = ref(null)
const dialogVisible = ref(false)
const userFormRef = ref(null)
const users = ref([])

// 搜索表单
const searchForm = ref({
  keyword: '',
  role: ''
})

// 添加用户表单
const userForm = ref({
  username: '',
  password: '',
  role: 'STUDENT',
  realName: '',
  email: '',
  phone: ''
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度必须在4-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

// 过滤后的用户列表
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    // 排除管理员用户
    if (user.role === 'ADMIN') return false
    
    // 按关键字搜索
    const keywordMatch = !searchForm.value.keyword ||
      user.username.toLowerCase().includes(searchForm.value.keyword.toLowerCase()) ||
      user.realName.toLowerCase().includes(searchForm.value.keyword.toLowerCase()) ||
      user.email.toLowerCase().includes(searchForm.value.keyword.toLowerCase())
    
    // 按角色筛选
    const roleMatch = !searchForm.value.role || user.role === searchForm.value.role
    
    return keywordMatch && roleMatch
  })
})

// 获取所有用户
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await request.get('/users/all')
    if (response.data.code === 200) {
      users.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('Error fetching users:', error)
  } finally {
    loading.value = false
  }
}

// 显示添加用户对话框
const showAddDialog = () => {
  // 重置表单
  userForm.value = {
    username: '',
    password: '',
    role: 'STUDENT',
    realName: '',
    email: '',
    phone: ''
  }
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  dialogVisible.value = true
}

// 添加用户
const handleAddUser = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    submitting.value = true
    
    const response = await request.post('/users/add', userForm.value)
    if (response.data.code === 200) {
      ElMessage.success('用户添加成功')
      dialogVisible.value = false
      // 重新获取用户列表
      fetchUsers()
    }
  } catch (error) {
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('添加用户失败')
    }
    console.error('Error adding user:', error)
  } finally {
    submitting.value = false
  }
}

// 搜索用户
const handleSearch = () => {
  // 搜索逻辑已通过computed属性实现
  console.log('Searching with:', searchForm.value)
}

// 删除用户
const handleDeleteUser = async (user) => {
  try {
    // 显示确认对话框
    await ElMessageBox.confirm(
      `确定要删除用户「${user.username}」吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 设置删除中状态
    deletingId.value = user.id
    
    // 发送删除请求
    const response = await request.delete(`/users/${user.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('用户删除成功')
      // 重新获取用户列表
      fetchUsers()
    }
  } catch (error) {
    // 如果是点击取消按钮，不显示错误信息
    if (error !== 'cancel') {
      if (error.response && error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('删除用户失败')
      }
    }
  } finally {
    // 重置删除状态
    deletingId.value = null
  }
}

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.add-user-btn {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

:deep(.el-table__row) {
  transition: background-color 0.3s;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>