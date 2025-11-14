<template>
  <div class="assignments-container">
    <!-- 头部区域 -->
    <div class="header">
      <h2>作业管理</h2>
      <div class="header-controls">
        <!-- 搜索框 -->
        <el-input
          v-model="searchQuery.keyword"
          placeholder="请输入作业标题关键词"
          style="width: 240px"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 课程选择 -->
        <!-- <el-select v-model="searchQuery.courseId" placeholder="请选择课程" @change="handleSearch">
          <el-option
            label="全部课程"
            :value="null"
          />
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.name"
            :value="course.id"
          />
        </el-select> -->
        
        <!-- 状态选择 -->
        <!-- <el-select v-model="searchQuery.status" placeholder="请选择状态" @change="handleSearch">
          <el-option
            label="全部状态"
            :value="null"
          />
          <el-option
            v-for="status in ['pending', 'submitted', 'overdue']"
            :key="status"
            :label="getStatusText(status)"
            :value="status"
          />
        </el-select> -->
        
        <!-- 添加按钮 (仅管理员和教师可见) -->
        <el-button 
          v-if="canAdd"
          type="primary" 
          @click="handleAdd"
        >
          <el-icon><Plus /></el-icon>
          发布作业
        </el-button>
      </div>
    </div>

    <!-- 作业列表 -->
    <el-table 
      :data="paginatedAssignments" 
      style="width: 100%" 
      v-loading="loading"
      :header-cell-style="{ background: '#f5f7fa' }"
      border
    >
      <el-table-column v-for="column in columns" :key="column.prop" v-bind="column" />
      <el-table-column label="操作" align="center">
        <template #default="{ row }">
          <el-space>
            <el-tooltip content="查看详情">
              <el-button
                type="primary"
                :icon="View"
                circle
                plain
                size="small"
                @click="handleView(row)"
              />
            </el-tooltip>
            
            <el-tooltip 
              v-if="canEdit(row)"
              content="编辑"
            >
              <el-button
                type="warning"
                :icon="Edit"
                circle
                plain
                size="small"
                @click="handleEdit(row)"
              />
            </el-tooltip>
            
            <el-tooltip 
              v-if="canDelete(row)"
              content="删除"
            >
              <el-button
                type="danger"
                :icon="Delete"
                circle
                plain
                size="small"
                @click="handleDelete(row)"
              />
            </el-tooltip>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="作业描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            rows="4"
          />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            placeholder="选择截止日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetForm">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      :title="viewingAssignment?.title"
      width="50%"
      destroy-on-close
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="课程">
          {{ viewingAssignment?.courseName }}
        </el-descriptions-item>
        <el-descriptions-item label="教师">
          {{ viewingAssignment?.teacherName }}
        </el-descriptions-item>
        <el-descriptions-item label="截止日期">
          <el-text :type="isOverdue(viewingAssignment?.deadline) ? 'danger' : 'primary'">
            {{ formatDate(viewingAssignment?.deadline) }}
          </el-text>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="getStatusType(viewingAssignment?.status, viewingAssignment?.deadline)"
            size="small"
            effect="light"
          >
            {{ getStatusText(viewingAssignment?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          <div class="description-content">
            {{ viewingAssignment?.description }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="提交情况">
          <el-button
            type="primary"
            link
            @click="handleViewSubmissions(viewingAssignment)"
          >
            查看学生提交情况
          </el-button>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailsVisible = false">关闭</el-button>
        <el-button
          v-if="canEdit(viewingAssignment)"
          type="primary"
          @click="handleEdit(viewingAssignment)"
        >
          编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, View, Edit, Delete } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const store = useStore()
const user = computed(() => store.state.user)

// 辅助函数
const isTeacher = computed(() => user.value.role === 'TEACHER')
const isAdmin = computed(() => user.value.role === 'ADMIN')
const isStudent = computed(() => user.value.role === 'STUDENT')

const canAdd = computed(() => isAdmin.value || isTeacher.value)

const canEdit = (assignment) => {
  if (!assignment) return false
  if (isAdmin.value) return true
  if (isTeacher.value) {
    return assignment.teacherId === user.value.id
  }
  return false
}

const canDelete = (assignment) => {
  if (!assignment) return false
  if (isAdmin.value) return true
  if (isTeacher.value) {
    return assignment.teacherId === user.value.id
  }
  return false
}

const isOverdue = (deadline) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

const getStatusType = (status, deadline) => {
  if (isOverdue(deadline)) return 'danger'
  const typeMap = {
    'pending': 'warning',
    'submitted': 'success',
    'overdue': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待提交',
    'submitted': '已提交',
    'overdue': '已逾期'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 数据
const assignments = ref([])
const courses = ref([])
const teachers = ref([])
const searchQuery = ref({
  keyword: '',
  courseId: null,
  status: null,
  teacherId: user.value.role === 'TEACHER' ? user.value.id : null
})
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const loading = ref(false)
const detailsVisible = ref(false)
const viewingAssignment = ref(null)

// 表单数据
const form = ref({
  id: null,
  title: '',
  description: '',
  courseId: null,
  deadline: null
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入作业描述', trigger: 'blur' },
    { min: 10, message: '描述至少需要 10 个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  deadline: [
    { required: true, message: '请选择截止日期', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value && value < new Date()) {
          callback(new Error('截止日期不能早于当前时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 表格列配置
const columns = [
  { prop: 'title', label: '标题', width: '200' },
  { prop: 'description', label: '描述', width: '300', showOverflowTooltip: true },
  { prop: 'courseName', label: '课程', width: '150' },
  { prop: 'teacherName', label: '教师', width: '120' },
  { 
    prop: 'status', 
    label: '状态', 
    width: '100',
    formatter: (row) => getStatusText(row.status)
  },
  { 
    prop: 'deadline', 
    label: '截止日期', 
    width: '160',
    formatter: (row) => formatDate(row.deadline)
  }
]

// 根据课程ID获取课程名称
const getCourseNameById = async (courseId) => {
  try {
    const response = await request.get(`/courses/${courseId}`)
    if (response.data.code === 200) {
      return response.data.data.name
    }
  } catch (error) {
    console.error('获取课程信息失败:', error)
  }
  return `未知课程`
}

// 根据教师ID获取教师名称
const getTeacherNameById = async (teacherId) => {
  try {
    const response = await request.get(`/users/${teacherId}`)
    if (response.data.code === 200) {
      return response.data.data.realName
    }
  } catch (error) {
    console.error('获取教师信息失败:', error)
  }
  return `教师${teacherId}`
}

// 获取作业列表
async function fetchAssignments() {
  try {
    loading.value = true
    const params = {
      ...searchQuery.value
    }
    
    // 过滤掉值为空的参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '' || params[key] === undefined) {
        delete params[key]
      }
    })

    // 如果有关键词搜索，确保是按标题搜索
    if (params.keyword) {
      params.titleKeyword = params.keyword
      delete params.keyword
    }

    const { data } = await request.get('/assignments', { params })
    if (data.code === 200) {
      assignments.value = data.data
      
      // 如果有搜索关键词，在前端再次过滤确保匹配
      if (searchQuery.value.keyword) {
        assignments.value = assignments.value.filter(assignment => 
          assignment.title.toLowerCase().includes(searchQuery.value.keyword.toLowerCase())
        )
      }

      // 获取教师和课程名称
      await Promise.all([
        ...assignments.value.map(async (assignment) => {
          assignment.courseName = await getCourseNameById(assignment.courseId)
          assignment.teacherName = await getTeacherNameById(assignment.teacherId)
        })
      ])
    } else {
      ElMessage.error(data.message || '获取作业列表失败')
    }
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    const { data } = await request.get('/courses')
    if (data.code === 200) {
      courses.value = data.data
    } else {
      ElMessage.error(data.message || '获取课程列表失败')
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 根据ID获取课程列表
const fetchCoursesByRole = async () => {
  try {
    let url = '/courses'
    if (isTeacher.value) {
      url = `/courses/teacher/${user.value.id}`
    } else if (isStudent.value) {
      url = `/courses/student/${user.value.id}`
    }
    const { data } = await request.get(url)
    if (data.code === 200) {
      courses.value = data.data
    } else {
      ElMessage.error(data.message || '获取课程列表失败')
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const response = await request.get('/users/teachers')
    if (response.data.code === 200) {
      teachers.value = response.data.data
      console.log('Teachers:', teachers.value) // 添加日志
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
  }
}

// 添加作业
const handleAdd = () => {
  dialogTitle.value = '发布作业'
  form.value = {
    id: null,
    title: '',
    description: '',
    courseId: null,
    deadline: null
  }
  dialogVisible.value = true
}

// 编辑作业
const handleEdit = (row) => {
  dialogTitle.value = '编辑作业'
  form.value = {
    id: row.id,
    title: row.title,
    description: row.description,
    courseId: row.courseId,
    deadline: row.deadline
  }
  dialogVisible.value = true
}

// 查看作业详情
const handleView = (row) => {
  if (user.value.role === 'STUDENT') {
    console.log('跳转到作业提交页面，作业ID:', row.id)
    router.push(`/homework-submission/${row.id}`).catch(err => {
      console.error('路由跳转失败:', err)
      ElMessage.error('页面跳转失败，请稍后重试')
    })
  } else {
    viewingAssignment.value = row
    detailsVisible.value = true
  }
}

// 删除作业
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个作业吗？', '提示', {
      type: 'warning'
    })
    const { data } = await request.delete(`/assignments/${row.id}`)
    if (data.code === 200) {
      ElMessage.success('删除成功')
      fetchAssignments()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除作业失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 查看提交情况
const handleViewSubmissions = (assignment) => {
  router.push({
    name: 'AssignmentSubmissions',
    params: { id: assignment.id }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
    form.value = {
      id: null,
      title: '',
      description: '',
      courseId: null,
      deadline: null
    }
  }
  dialogVisible.value = false
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    const isEdit = form.value.id !== null
    
    // 构建提交数据
    const submitData = {
      ...form.value,
      teacherId: user.value.id,  // 确保设置教师ID
      deadline: form.value.deadline ? new Date(form.value.deadline).toISOString() : null,  // 格式化日期
      courseId: Number(form.value.courseId)  // 确保 courseId 是数字
    }
    
    console.log('提交的表单数据:', submitData)
    
    const { data } = await request[isEdit ? 'put' : 'post'](
      `/assignments${isEdit ? `/${submitData.id}` : ''}`,
      submitData
    )
    
    if (data.code === 200) {
      ElMessage.success(isEdit ? '更新成功' : '添加成功')
      resetForm()
      fetchAssignments()
    } else {
      ElMessage.error(data.message || (isEdit ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error(error.message || '提交失败，请检查表单数据')
  }
}

// 搜索方法
const handleSearch = () => {
  // 防抖处理，避免频繁请求
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    // 过滤空白字符
    searchQuery.value.keyword = searchQuery.value.keyword.trim()
    fetchAssignments()
  }, 500)
}

// 重置搜索
const resetSearch = () => {
  searchQuery.value = {
    keyword: '',
    courseId: null,
    status: null,
    teacherId: user.value.role === 'TEACHER' ? user.value.id : null
  }
  fetchAssignments()
}

// 监听搜索条件变化
watch([() => searchQuery.value.courseId, () => searchQuery.value.status], () => {
  handleSearch()
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 计算当前页的数据
const paginatedAssignments = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return assignments.value.slice(start, end)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 初始化
onMounted(() => {
  fetchCoursesByRole()
  fetchAssignments()
  fetchTeachers()
})

// 添加防抖定时器
let searchTimer = null
</script>

<style scoped>
.assignments-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.header-controls {
  display: flex;
  gap: 10px;
}

:deep(.el-table) {
  margin-top: 16px;
  border-radius: 4px;
}

:deep(.el-table__header) {
  font-weight: bold;
}

:deep(.el-button-group .el-button--circle + .el-button--circle) {
  margin-left: 4px;
}

.description-text {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.description-container:hover .description-text {
  color: var(--el-color-primary);
}

.assignment-detail {
  padding: 20px;
}

.detail-item {
  margin-bottom: 20px;
}

.detail-item h4 {
  margin: 0 0 8px 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.detail-item p {
  margin: 0;
  font-size: 16px;
}

.detail-item .description {
  white-space: pre-wrap;
  line-height: 1.6;
}

:deep(.assignment-detail-dialog) {
  .el-dialog__body {
    padding: 20px;
  }
  
  .el-descriptions {
    padding: 0;
  }
  
  .el-descriptions__label {
    width: 120px;
    font-weight: bold;
    color: var(--el-text-color-regular);
    background-color: var(--el-fill-color-light);
  }
  
  .el-descriptions__content {
    padding: 12px 16px;
  }
}

.description-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.el-descriptions {
  margin: 20px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
