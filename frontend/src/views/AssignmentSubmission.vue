<template>
  <div class="homework-submission">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的作业</h2>
        </div>
      </template>

      <!-- 作业列表 -->
      <el-table :data="assignments" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="作业标题" />
        <el-table-column prop="courseName" label="所属课程" />
        <el-table-column prop="deadline" label="截止日期">
          <template #default="scope">
            {{ formatDate(scope.row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button text type="primary" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button 
              text 
              type="success" 
              @click="handleSubmit(scope.row)"
              :disabled="scope.row.status === 'submitted' || scope.row.status === 'graded'"
            >
              提交
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 作业详情对话框 -->
      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="50%">
        <div v-if="currentAssignment">
          <h3>{{ currentAssignment.title }}</h3>
          <p class="description">{{ currentAssignment.description }}</p>
          <div class="info">
            <p><strong>课程：</strong>{{ currentAssignment.courseName }}</p>
            <p><strong>截止日期：</strong>{{ formatDate(currentAssignment.deadline) }}</p>
            <p><strong>状态：</strong>{{ getStatusText(currentAssignment.status) }}</p>
            <template v-if="currentAssignment.status === 'graded'">
              <p><strong>分数：</strong>{{ currentAssignment.score }}</p>
              <p><strong>反馈：</strong>{{ currentAssignment.feedback }}</p>
            </template>
          </div>
        </div>
      </el-dialog>

      <!-- 提交作业对话框 -->
      <el-dialog v-model="submitDialogVisible" title="提交作业" width="50%">
        <el-form ref="submitForm" :model="submitForm" :rules="rules" label-width="100px">
          <el-form-item label="作业内容" prop="content">
            <el-input
              v-model="submitForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入作业内容"
            />
          </el-form-item>
          <el-form-item label="附件">
            <el-upload
              action="/files/upload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
            >
              <el-button type="primary">上传文件</el-button>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="submitDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitHomework">提交</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import request from '@/utils/request'
import { formatDate } from '@/utils/format'

const store = useStore()
const loading = ref(false)
const assignments = ref([])
const dialogVisible = ref(false)
const submitDialogVisible = ref(false)
const currentAssignment = ref(null)
const dialogTitle = ref('')

const submitForm = ref({
  content: '',
  attachments: []
})

const rules = {
  content: [
    { required: true, message: '请输入作业内容', trigger: 'blur' }
  ]
}

// 获取作业列表
const fetchAssignments = async () => {
  loading.value = true
  try {
    const userId = store.state.user.id
    const { data } = await request.get(`/assignment/student/${userId}`)
    assignments.value = data.data
  } catch (error) {
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待提交',
    submitted: '已提交',
    graded: '已评分',
    overdue: '已逾期'
  }
  return texts[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    submitted: 'primary',
    graded: 'success',
    overdue: 'danger'
  }
  return types[status] || 'info'
}

// 查看作业
const handleView = (assignment) => {
  currentAssignment.value = assignment
  dialogTitle.value = '作业详情'
  dialogVisible.value = true
}

// 提交作业
const handleSubmit = (assignment) => {
  currentAssignment.value = assignment
  initSubmitForm()
  submitDialogVisible.value = true
}

// 处理文件上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    submitForm.value.attachments.push(response.data)
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response.message || '文件上传失败')
  }
}

// 处理文件上传失败
const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

// 提交作业
const submitHomework = async () => {
  try {
    await submitForm.value.validate()
    const submission = {
      assignmentId: currentAssignment.value.id,
      content: submitForm.value.content,
      attachments: submitForm.value.attachments
    }
    await request.post('/assignment-submission', submission)
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    fetchAssignments()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('提交失败')
    }
  }
}

// 上传文件之前的钩子
const beforeUpload = (file) => {
  // 检查文件大小（限制为10MB）
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  // 检查文件类型
  const allowedTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'text/plain'
  ]
  
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只能上传Office文档、PDF或文本文件')
    return false
  }
  
  return true
}

// 初始化表单数据
const initSubmitForm = () => {
  submitForm.value = {
    content: '',
    attachments: []
  }
}

onMounted(() => {
  fetchAssignments()
})
</script>

<style scoped>
.homework-submission {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description {
  white-space: pre-wrap;
  margin: 20px 0;
}

.info {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.info p {
  margin: 5px 0;
}
</style>
