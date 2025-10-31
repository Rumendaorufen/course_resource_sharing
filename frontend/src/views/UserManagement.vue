<template>
  <div class="user-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="left-section">
            <span>用户管理</span>
            <el-input
              v-model="searchUsername"
              placeholder="搜索用户名"
              class="search-input"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="filterRole"
              placeholder="选择角色筛选"
              clearable
              class="filter-select"
            >
              <el-option label="管理员" value="ADMIN" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </div>
          <el-button type="primary" @click="handleAdd">添加用户</el-button>
        </div>
      </template>

      <!-- 用户列表 -->
      <el-table :data="filteredUsers" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">{{ getRoleLabel(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="enabled" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column prop="lastLoginTime" label="最后登录时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next"
          :total="filteredTotal"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="30%"
      @close="closeDialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" />
        </el-form-item>
        <el-form-item 
          label="密码" 
          prop="password"
        >
          <el-input 
            v-model="formData.password" 
            type="password"
            :placeholder="dialogTitle === '编辑用户' ? '不修改请留空' : '请输入密码'"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '../utils/axios'
import { Search } from '@element-plus/icons-vue'

// 响应式数据
const users = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const formData = ref({
  username: '',
  password: '',
  realName: '',
  role: '',
  email: '',
  phone: ''
})

// 动态验证规则
const formRules = computed(() => {
  const isEdit = dialogTitle.value === '编辑用户'
  return {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
    ],
    password: isEdit 
      ? [{ min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }]
      : [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
    role: [
      { required: true, message: '请选择角色', trigger: 'change' }
    ],
    realName: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  }
})

// 添加角色筛选相关的响应式数据
const filterRole = ref('')

// 添加搜索相关的响应式数据
const searchUsername = ref('')

// 修改过滤用户数据的计算属性
const filteredUsers = computed(() => {
  let filtered = users.value

  // 按用户名搜索
  if (searchUsername.value) {
    filtered = filtered.filter(user => 
      user.username.toLowerCase().includes(searchUsername.value.toLowerCase())
    )
  }

  // 按角色筛选
  if (filterRole.value) {
    filtered = filtered.filter(user => user.role === filterRole.value)
  }

  // 分页处理
  return filtered.slice(
    (currentPage.value - 1) * pageSize.value,
    currentPage.value * pageSize.value
  )
})

// 修改总数计算属性
const filteredTotal = computed(() => {
  let filtered = users.value

  // 按用户名搜索
  if (searchUsername.value) {
    filtered = filtered.filter(user => 
      user.username.toLowerCase().includes(searchUsername.value.toLowerCase())
    )
  }

  // 按角色筛选
  if (filterRole.value) {
    filtered = filtered.filter(user => user.role === filterRole.value)
  }

  return filtered.length
})

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/users', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    console.log('Response:', response) // 添加日志
    if (response.code === 200) {
      const { content, totalElements } = response.data
      users.value = content
      total.value = totalElements
      console.log('Users:', users.value) // 添加日志
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 角色标签相关
const getRoleLabel = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'STUDENT': '学生'
  }
  return roleMap[role] || role
}

const getRoleType = (role) => {
  const typeMap = {
    'ADMIN': 'danger',
    'TEACHER': 'warning',
    'STUDENT': 'success'
  }
  return typeMap[role] || 'info'
}

// 处理添加用户
const handleAdd = () => {
  dialogTitle.value = '添加用户'
  formData.value = {
    username: '',
    password: '',
    realName: '',
    role: '',
    email: '',
    phone: ''
  }
  dialogVisible.value = true
}

// 处理编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  formData.value = {
    id: row.id,
    username: row.username,
    password: '', // 编辑时不显示密码
    realName: row.realName,
    role: row.role,
    email: row.email,
    phone: row.phone
  }
  dialogVisible.value = true
}

// 处理删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除用户 ${row.username} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  .then(async () => {
    try {
      const response = await axios.delete(`/api/users/${row.id}`)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchUsers()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  })
  .catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    const isEdit = dialogTitle.value === '编辑用户'
    
    // 准备提交数据
    const submitData = Object.entries(formData.value).reduce((acc, [key, value]) => {
      if (value !== null && value !== '' && value !== undefined) {
        acc[key] = value
      }
      return acc
    }, {})

    // 如果是编辑用户且没有输入密码，删除密码字段
    if (isEdit && !submitData.password) {
      delete submitData.password
    }

    // 如果是添加用户，添加创建时间和更新时间
    if (!isEdit) {
      const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
      submitData.createTime = now
      submitData.updateTime = now
      submitData.enabled = true
    } else {
      submitData.updateTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
    }

    const response = await axios({
      method: isEdit ? 'put' : 'post',
      url: `/api/users${isEdit ? `/${formData.value.id}` : ''}`,
      data: submitData
    })
    
    if (response.code === 200) {
      ElMessage.success(isEdit ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      fetchUsers()
    } else {
      ElMessage.error(response.message || (isEdit ? '编辑失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败')
  }
}

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 初始化
onMounted(() => {
  console.log('Component mounted') // 添加日志
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  margin-top: 20px;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.filter-select {
  width: 150px;
}

.search-input {
  width: 200px;
}
</style>
