<template>
  <div class="user-management">
    <h2>用户管理</h2>
    
    <!-- 数据统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon success">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-number">{{ totalUsers }}</div>
              <div class="card-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon primary">
              <el-icon><UserTag /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-number">{{ teacherCount }}</div>
              <div class="card-label">教师用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon info">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-number">{{ studentCount }}</div>
              <div class="card-label">学生用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon warning">
              <el-icon><UserCheck /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-number">{{ activeUsers }}</div>
              <div class="card-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 数据可视化区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户类型分布</span>
            </div>
          </template>
          <div ref="userTypeChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户活跃度趋势</span>
            </div>
          </template>
          <div ref="userActivityChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '../utils/request'

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const deletingId = ref(null)
const dialogVisible = ref(false)
const editingUser = ref(null)
const userFormRef = ref(null)
const users = ref([])

// 图表引用
const userTypeChart = ref(null)
const userActivityChart = ref(null)
const userTypeChartInstance = ref(null)
const userActivityChartInstance = ref(null)

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

// 统计数据计算属性
const totalUsers = computed(() => {
  return users.value.filter(user => user.role !== 'ADMIN').length
})
const studentCount = computed(() => {
  return users.value.filter(user => user.role === 'STUDENT').length
})
const teacherCount = computed(() => {
  return users.value.filter(user => user.role === 'TEACHER').length
})
const activeUsers = computed(() => {
  // 假设根据用户创建时间和当前时间的差距来判断活跃状态
  const now = new Date()
  const thirtyDaysAgo = new Date(now.setDate(now.getDate() - 30))
  return users.value.filter(user => {
    const createTime = new Date(user.createTime || Date.now())
    return createTime > thirtyDaysAgo && user.role !== 'ADMIN'
  }).length
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
    } else {
      // 处理非200状态码但没有抛出异常的情况
      if (response.data && response.data.message) {
        ElMessage.error(response.data.message)
      } else {
        ElMessage.error('删除用户失败：未知错误')
      }
    }
  } catch (error) {
    // 如果是点击取消按钮，不显示错误信息
    if (error !== 'cancel') {
      console.log('删除用户错误详情:', error)
      // 增强错误处理逻辑，确保所有类型的错误都能正确显示
      if (error.response) {
        // 服务器返回了错误响应
        if (error.response.data && error.response.data.message) {
          ElMessage.error(error.response.data.message)
        } else if (error.response.data) {
          ElMessage.error(JSON.stringify(error.response.data))
        } else {
          ElMessage.error(`请求失败: ${error.response.status}`)
        }
      } else if (error.request) {
        // 请求已发送但没有收到响应
        ElMessage.error('网络错误，请检查网络连接')
      } else if (error.message) {
        // 请求配置出错
        ElMessage.error(error.message)
      } else {
        // 其他未知错误
        ElMessage.error('删除用户失败')
      }
    }
  } finally {
    // 重置删除状态
    deletingId.value = null
  }
}

// 初始化用户类型分布图表（饼图）
const initUserTypeChart = () => {
  if (!userTypeChart.value) return
  
  // 销毁已有实例
  if (userTypeChartInstance.value) {
    userTypeChartInstance.value.dispose()
  }
  
  userTypeChartInstance.value = echarts.init(userTypeChart.value)
  
  // 统计用户类型分布
  const roleStats = {
    '学生': users.value.filter(user => user.role === 'STUDENT').length,
    '教师': users.value.filter(user => user.role === 'TEACHER').length,
    '管理员': users.value.filter(user => user.role === 'ADMIN').length
  }
  
  const data = Object.entries(roleStats).map(([key, value]) => ({
    name: key,
    value
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '用户类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ],
    color: ['#67C23A', '#409EFF', '#E6A23C']
  }
  
  userTypeChartInstance.value.setOption(option)
}

// 初始化用户活跃度趋势图表（折线图）
const initUserActivityChart = () => {
  if (!userActivityChart.value) return
  
  // 销毁已有实例
  if (userActivityChartInstance.value) {
    userActivityChartInstance.value.dispose()
  }
  
  userActivityChartInstance.value = echarts.init(userActivityChart.value)
  
  // 生成最近6个月的月份数组
  const months = []
  const now = new Date()
  const monthData = []
  
  // 初始化过去6个月的数据结构
  for (let i = 5; i >= 0; i--) {
    const monthDate = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const monthName = `${monthDate.getMonth() + 1}月`
    const monthKey = `${monthDate.getFullYear()}-${String(monthDate.getMonth() + 1).padStart(2, '0')}`
    
    months.push(monthName)
    monthData.push({
      monthKey,
      monthName,
      startDate: new Date(monthDate.getFullYear(), monthDate.getMonth(), 1),
      endDate: new Date(monthDate.getFullYear(), monthDate.getMonth() + 1, 0, 23, 59, 59)
    })
  }
  
  // 基于真实用户数据统计活跃度，按创建时间分组
  const studentData = monthData.map(month => {
    return users.value.filter(user => {
      if (user.role !== 'STUDENT' || !user.createTime) return false
      const createDate = new Date(user.createTime)
      return createDate >= month.startDate && createDate <= month.endDate
    }).length
  })
  
  const teacherData = monthData.map(month => {
    return users.value.filter(user => {
      if (user.role !== 'TEACHER' || !user.createTime) return false
      const createDate = new Date(user.createTime)
      return createDate >= month.startDate && createDate <= month.endDate
    }).length
  })
  
  // 移除默认数据，仅使用从后端获取的真实用户数据
  console.log('User activity stats calculated:', { studentData, teacherData })
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['学生', '教师']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '学生',
        type: 'line',
        stack: '总量',
        data: studentData,
        smooth: true,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      },
      {
        name: '教师',
        type: 'line',
        stack: '总量',
        data: teacherData,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      }
    ]
  }
  
  userActivityChartInstance.value.setOption(option)
}

// 更新图表
const updateCharts = () => {
  // 延迟执行以确保DOM已更新
  setTimeout(() => {
    initUserTypeChart()
    initUserActivityChart()
  }, 100)
}

// 监听用户数据变化，更新图表
watch(() => users.value.length, () => {
  updateCharts()
})

// 监听窗口大小变化
const handleResize = () => {
  if (userTypeChartInstance.value) {
    userTypeChartInstance.value.resize()
  }
  if (userActivityChartInstance.value) {
    userActivityChartInstance.value.resize()
  }
}

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUsers()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (userTypeChartInstance.value) {
    userTypeChartInstance.value.dispose()
  }
  if (userActivityChartInstance.value) {
    userActivityChartInstance.value.dispose()
  }
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
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

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 20px;
}

.charts-section {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 15px;
}

.card-icon.primary {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.card-icon.success {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.card-icon.warning {
  background-color: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.card-icon.info {
  background-color: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.card-text {
  flex: 1;
}

.card-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.card-label {
  color: #909399;
  font-size: 14px;
}

/* 图表容器样式 */
.chart-container {
  width: 100%;
  height: 350px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
  
  .card-number {
    font-size: 24px;
  }
  
  .card-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}
</style>