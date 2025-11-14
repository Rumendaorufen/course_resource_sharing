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
        <el-select v-model="searchForm.classname" placeholder="班级">
          <el-option value="" label="全部" />
          <el-option v-for="className in classNames" :key="className" :label="className" :value="className" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 用户列表 -->
    <el-table v-loading="loading" :data="paginatedUsers" style="width: 100%">
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
      <el-table-column prop="classname" label="班级" width="120">
        <template #default="scope">
          <span v-if="scope.row.classname">{{ scope.row.classname }}</span>
          <span v-else-if="scope.row.role === 'STUDENT'">未设置</span>
          <span v-else>-</span>
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
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <!-- 不能编辑管理员用户 -->
          <template v-if="scope.row.role !== 'ADMIN'">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEditUser(scope.row)"
              style="margin-right: 8px"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDeleteUser(scope.row)"
              :loading="deletingId === scope.row.id"
            >
              删除
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 用户对话框（添加/编辑共用） -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingUser ? '编辑用户' : '添加用户'"
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
          <el-input 
            v-model="userForm.password" 
            type="password" 
            :placeholder="editingUser ? '请输入密码（留空表示不修改）' : '请输入密码'" 
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" @change="handleRoleChange">
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
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号（可选）" />
        </el-form-item>
        <!-- 班级字段，仅学生用户显示 -->
        <el-form-item v-if="userForm.role === 'STUDENT'" label="班级" prop="classname">
          <el-select 
            v-model="selectedClass" 
            placeholder="请选择班级" 
            @change="handleClassChange"
            style="width: 100%"
          >
            <el-option value="" label="请选择班级" />
            <el-option v-for="className in classNames" :key="className" :label="className" :value="className" />
            <el-option value="NEW_CLASS" label="添加新班级" />
          </el-select>
          <el-input 
            v-if="selectedClass === 'NEW_CLASS'" 
            v-model="newClassName" 
            placeholder="请输入新班级名称" 
            style="margin-top: 10px;"
            @input="updateClassnameFromNew()"
          />
        </el-form-item>
      </el-form>
      <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitUser" :loading="submitting">确定</el-button>
        </template>
    </el-dialog>
    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredUsers.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
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
const editingUser = ref(null)
const userFormRef = ref(null)
const users = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const classNames = ref([])

// 搜索表单
const searchForm = ref({
  keyword: '',
  role: '',
  classname: ''
})

// 添加用户表单
const userForm = ref({
  username: '',
  password: '',
  role: 'STUDENT',
  realName: '',
  email: '',
  phone: '',
  classname: ''
})

// 班级选择相关
const selectedClass = ref('')
const newClassName = ref('')

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度必须在4-20个字符之间', trigger: 'blur' }
  ],
  password: [
    {
      required: false, // 编辑模式下密码始终不是必填项
      message: '请输入密码',
      trigger: 'blur'
    },
    {
      min: 6,
      max: 20,
      message: '密码长度必须在6-20个字符之间',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (!value) {
          callback(); // 密码为空时通过验证，保持原密码
        } else if (value.length >= 6 && value.length <= 20) {
          callback();
        } else {
          callback(new Error('密码长度必须在6-20个字符之间'));
        }
      }
    }
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
  ],
  phone: [
    {
      required: false,
      message: '请输入手机号',
      trigger: 'blur'
    },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (!value) {
          callback(); // 手机号为空时通过验证
        } else if (/^1[3-9]\d{9}$/.test(value)) {
          callback();
        } else {
          callback(new Error('请输入正确的手机号格式'));
        }
      }
    }
  ],
  classname: [
    { 
      validator: (rule, value, callback) => {
        // 无论是添加还是编辑模式，学生用户都必须填写班级信息
        if (userForm.value.role === 'STUDENT' && (!value || value.trim() === '')) {
          callback(new Error('学生用户必须填写班级信息'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 处理角色变更
const handleRoleChange = () => {
  // 如果切换到非学生角色，清空班级信息
  if (userForm.value.role !== 'STUDENT') {
    userForm.value.classname = '';
    selectedClass.value = '';
    newClassName.value = '';
  }
  // 重置表单验证状态
  if (userFormRef.value) {
    userFormRef.value.clearValidate('classname');
  }
}

// 处理班级选择变化
const handleClassChange = () => {
  if (selectedClass.value === '') {
    userForm.value.classname = '';
    newClassName.value = '';
  } else if (selectedClass.value === 'NEW_CLASS') {
    userForm.value.classname = '';
    newClassName.value = '';
  } else {
    userForm.value.classname = selectedClass.value;
    newClassName.value = '';
  }
}

// 从新班级输入更新classname
const updateClassnameFromNew = () => {
  userForm.value.classname = newClassName.value;
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
    
    // 按班级筛选
    const classMatch = !searchForm.value.classname || user.classname === searchForm.value.classname
    
    return keywordMatch && roleMatch && classMatch
  })
})

// 分页后的用户列表
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// 提取所有班级名称
const extractClassNames = () => {
  const classes = new Set()
  users.value.forEach(user => {
    if (user.role === 'STUDENT' && user.classname && user.classname.trim()) {
      classes.add(user.classname.trim())
    }
  })
  classNames.value = Array.from(classes).sort()
}

// 获取所有用户
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await request.get('/users/all')
    if (response.data.code === 200) {
      users.value = response.data.data
      // 提取班级名称列表
      extractClassNames()
      // 重置到第一页
      currentPage.value = 1
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
  editingUser.value = null
  // 重置表单
  userForm.value = {
    username: '',
    password: '',
    role: 'STUDENT',
    realName: '',
    email: '',
    phone: '',
    classname: ''
  }
  selectedClass.value = ''
  newClassName.value = ''
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  dialogVisible.value = true
}

// 提交用户表单（添加或编辑）
const handleSubmitUser = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    submitting.value = true
    
    let response;
    if (editingUser.value) {
      // 编辑模式
      response = await request.put(`/users/${editingUser.value.id}`, userForm.value)
    } else {
      // 添加模式
      response = await request.post('/users/add', userForm.value)
    }
    
    if (response.data.code === 200) {
      ElMessage.success(editingUser.value ? '用户编辑成功' : '用户添加成功')
      dialogVisible.value = false
      // 重新获取用户列表
      fetchUsers()
    }
  } catch (error) {
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error(editingUser.value ? '编辑用户失败' : '添加用户失败')
    }
    console.error(editingUser.value ? 'Error updating user:' : 'Error adding user:', error)
  } finally {
    submitting.value = false
  }
}

// 编辑用户
const handleEditUser = (user) => {
  editingUser.value = user
  // 填充表单数据
  userForm.value = {
    username: user.username,
    password: '', // 编辑时密码为空，不修改密码
    role: user.role,
    realName: user.realName,
    email: user.email,
    phone: user.phone || '',
    classname: user.classname || ''
  }
  // 设置班级选择
  selectedClass.value = user.classname || ''
  newClassName.value = ''
  // 打开对话框
  dialogVisible.value = true
}

// 搜索用户
const handleSearch = () => {
  // 搜索时重置到第一页
  currentPage.value = 1
  console.log('Searching with:', searchForm.value)
}

// 分页处理函数
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (val) => {
  currentPage.value = val
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table__row) {
  transition: background-color 0.3s;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>