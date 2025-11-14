<template>
  <div class="homework-submission" v-loading="loading">
    <div class="header">
      <h2>作业提交</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <template v-if="assignment">
      <!-- 作业信息 -->
      <el-card class="assignment-info">
        <template #header>
          <div class="card-header">
            <span>作业详情</span>
          </div>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">{{ assignment.title }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ assignment.description }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ formatDate(assignment.deadline) }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ assignment.courseName || '暂无课程信息' }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ assignment.teacherName || '暂无教师信息' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="isSubmissionDeadlinePassed ? 'danger' : 'success'">
              {{ isSubmissionDeadlinePassed ? '已截止' : '进行中' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      <!-- 教师查看提交记录 -->
<el-card v-if="store.state.user.role === 'TEACHER'" class="submission-list">
  <template #header>
    <div class="card-header">
      <span>提交记录</span>
      <el-button type="primary" size="small" @click="fetchSubmissions">刷新</el-button>
    </div>
  </template>
  
  <el-table :data="submissions" border stripe>
    <el-table-column prop="studentName" label="学生姓名" />
    <el-table-column prop="submitTime" label="提交时间">
      <template #default="{ row }">
        {{ formatDate(row.submitTime) }}
      </template>
    </el-table-column>
    <el-table-column prop="content" label="作业内容" show-overflow-tooltip />
    <el-table-column prop="attachmentUrl" label="附件">
      <template #default="{ row }">
        <el-link v-if="row.attachmentUrl" :href="row.attachmentUrl" target="_blank">
          查看附件
        </el-link>
        <span v-else>无附件</span>
      </template>
    </el-table-column>
    <el-table-column prop="status" label="状态">
      <template #default="{ row }">
        <el-tag :type="row.status === 'GRADED' ? 'success' : 'warning'">
          {{ row.status === 'GRADED' ? '已批改' : '未批改' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="score" label="分数" />
    <el-table-column label="操作" width="200">
      <template #default="{ row }">
        <el-button 
          type="primary" 
          size="small" 
          @click="handleGrade(row)"
          :disabled="row.status === 'GRADED'"
        >
          批改
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</el-card>    

      <!-- 提交表单 -->
      <el-card class="submission-form" v-loading="submitting">
        <template #header>
          <div class="card-header">
            <span>提交作业</span>
            <el-tag 
              :type="isSubmissionDeadlinePassed ? 'danger' : 'success'"
              size="small"
            >
              {{ isSubmissionDeadlinePassed ? '已截止' : '进行中' }}
            </el-tag>
          </div>
        </template>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="4"
              placeholder="请输入作业内容"
              :disabled="isSubmissionDeadlinePassed"
            />
          </el-form-item>
          <el-form-item label="附件">
            <el-upload
              ref="uploadRef"
              class="upload-demo"
              :auto-upload="true"
              :show-file-list="true"
              :on-change="handleFileChange"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemoveFile"
              :before-upload="beforeUploadFile"
              :http-request="customUpload"
              :limit="1"
              :file-list="fileList"
              accept=".pdf,.doc,.docx,.txt,.zip,.rar"
            >
              <template #trigger>
                <el-button type="primary">
                  选择文件
                </el-button>
              </template>
              <template #tip>
                <div class="el-upload__tip">
                  文件大小不能超过100MB，选择后自动上传
                </div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitHomework" :loading="submitting" :disabled="isSubmissionDeadlinePassed">
              {{ form.id ? '更新提交' : '提交作业' }}
            </el-button>
            <el-button @click="$router.push('/student-assignments')">返回</el-button>
            <el-button @click="cancelSubmit">取消提交</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const store = useStore()
const baseUrl = import.meta.env.VITE_API_URL

// 数据
const loading = ref(false)
const submitting = ref(false)
const assignment = ref(null)
const form = ref({
  content: '',
  attachmentUrl: '',
  attachmentName: '',
  attachmentSize: 0
})

// 表单引用
const formRef = ref(null)

// 表单验证规则
const rules = {
  content: [
    { required: true, message: '请输入作业内容', trigger: 'blur' },
    { min: 1, max: 5000, message: '内容长度应在1-5000字符之间', trigger: 'blur' }
  ]
}

// 验证规则
const validateBeforeSubmit = async () => {
  if (!formRef.value) return false
  
  try {
    await formRef.value.validate()
    return true
  } catch (error) {
    return false
  }
}

// 提交前验证
const submitHomework = async () => {
  // 检查是否已截止
  if (isSubmissionDeadlinePassed.value) {
    ElMessage.error('作业已截止，无法提交')
    return
  }

  // 表单验证
  const isValid = await validateBeforeSubmit()
  if (!isValid) {
    ElMessage.error('请完善表单信息')
    return
  }

  submitting.value = true
  try {
    const studentId = store.state.user.id
    // 构建提交数据
    const submissionData = {
      assignmentId: route.params.id,
      content: form.value.content,
      attachmentUrl: form.value.attachmentUrl || '',
      attachmentName: form.value.attachmentName || '',
      attachmentSize: form.value.attachmentSize || 0,
      status: 'SUBMITTED'
    }
    
    const response = await request.post(`/api/homework-submissions/student/${studentId}`, submissionData)
    
    if (response.data.code === 200) {  
      ElMessage.success('作业提交成功')
      // 触发父组件刷新作业列表
      const assignmentsView = router.currentRoute.value.matched.find(
        record => record.name === 'student-assignments'
      )?.instances?.default
      if (assignmentsView?.loadAssignments) {
        await assignmentsView.loadAssignments()
      }
      router.push('/student-assignments')
    } else {
      ElMessage.error(response.data.message || '作业提交失败')  
    }
  } catch (error) {
    console.error('提交作业失败:', error)
    ElMessage.error(error.response?.data?.message || '作业提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  // 重置表单引用
  formRef.value?.resetFields()
  
  // 清空所有表单数据
  form.value = {
    content: '',
    attachmentUrl: '',
    attachmentName: '',
    attachmentSize: 0
  }
  
  // 清空文件列表
  fileList.value = []
}

// 取消提交
const cancelSubmit = () => {
  ElMessageBox.confirm('确定要取消提交吗？已填写的内容将会丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    resetForm()
    router.push('/student-assignments')
  }).catch(() => {})
}

// 上传相关
const headers = computed(() => ({
  Authorization: `Bearer ${store.state.token}`
}))

// 获取作业信息
const fetchAssignment = async () => {
  if (!route.params.id) {
    ElMessage.error('缺少作业ID')
    router.back()
    return
  }

  loading.value = true
  try {
    const assignmentId = route.params.id
    console.log('获取作业信息，ID:', assignmentId)
    
    // 获取作业基本信息
    const userId = store.state.user.id
    const { data: assignmentRes } = await request.get(`/api/assignments/student/${userId}/assignment/${assignmentId}`)
    console.log('作业基本信息响应:', assignmentRes)
    
    if (!assignmentRes || !assignmentRes.data) {
      throw new Error('作业不存在')
    }

    assignment.value = assignmentRes.data

    // 获取提交信息
    const { data: submissionRes } = await request.get(`/api/homework-submissions/student/${userId}/assignment/${assignmentId}/latest`)
    console.log('作业提交信息响应:', submissionRes)
    
    if (submissionRes && submissionRes.data) {
      form.value.content = submissionRes.data.content || ''
      
      if (submissionRes.data.attachmentUrl) {
        form.value.attachmentUrl = submissionRes.data.attachmentUrl
        form.value.attachmentName = submissionRes.data.attachmentName || ''
        form.value.attachmentSize = submissionRes.data.attachmentSize || 0
        
        fileList.value = [{ name: form.value.attachmentName || '附件', url: form.value.attachmentUrl }]
      }
    }
  } catch (error) {
    console.error('获取作业信息失败:', error)
    ElMessage.error(error.message || '获取作业信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

// 文件上传相关
  const fileList = ref([])
  const uploading = ref(false)
  const uploadRef = ref(null)

  // 处理文件变化
const handleFileChange = (file, fileList) => {
  // 限制只能上传一个文件
  if (fileList.length > 1) {
    ElMessage.warning('只能上传一个文件');
    return fileList.slice(-1);
  }
  return fileList;
};

// 上传前检查
const beforeUploadFile = (file) => {
  // 检查文件大小
  const isLt100M = file.size / 1024 / 1024 < 100;
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB!');
    return false;
  }
  
  // 检查文件类型
  const allowedTypes = ['.pdf', '.doc', '.docx', '.txt', '.zip', '.rar'];
  const extension = '.' + file.name.split('.').pop().toLowerCase();
  if (!allowedTypes.includes(extension)) {
    ElMessage.error(`只支持以下文件类型: ${allowedTypes.join(', ')}`);
    return false;
  }
  
  return true;
};

// 自定义上传方法
const customUpload = async (options) => {
  uploading.value = true;
  const file = options.file;
  
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: `Bearer ${store.state.token}`
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        console.log(`上传进度: ${percentCompleted}%`);
        options.onProgress({ percent: percentCompleted });
      }
    });
    
    if (response.data.code === 200) {
      options.onSuccess(response.data);
    } else {
      options.onError(new Error(response.data.message || '文件上传失败'));
    }
  } catch (error) {
    console.error('文件上传失败:', error);
    options.onError(error);
  } finally {
    uploading.value = false;
  }
};

// 上传成功处理
const handleUploadSuccess = (response, file) => {
  ElMessage.success('文件上传成功');
  form.value.attachmentUrl = response.data;
  form.value.attachmentName = file.name;
  form.value.attachmentSize = file.size;
  
  // 更新文件列表
  fileList.value = [{
    name: file.name,
    url: response.data,
    size: file.size,
    uid: file.uid,
    status: 'success'
  }];
};

// 移除文件
const handleRemoveFile = () => {
  form.value.attachmentUrl = '';
  form.value.attachmentName = '';
  form.value.attachmentSize = 0;
  fileList.value = [];
};

// 生命周期
onMounted(() => {
  console.log('组件挂载，路由参数:', route.params)
  fetchAssignment()
  if (store.state.user.role === 'TEACHER') {
    fetchSubmissions()
  }
})

const isSubmissionDeadlinePassed = computed(() => {
  if (!assignment.value?.deadline) return false
  return new Date(assignment.value.deadline) < new Date()
})

const submissions = ref([])
const fetchSubmissions = async () => {
  try {
    loading.value = true
    const response = await request.get(`/api/homework-submissions/homework/${route.params.id}`)
    if (response.data.code === 200) {
      submissions.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取提交记录失败')
    }
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('获取提交记录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.homework-submission {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.submission-list {
  margin-top: 20px;
}

.submission-form {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.assignment-info {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-card {
  margin-top: 20px;
}

.el-timeline {
  margin-top: 20px;
  padding: 20px;
}

.el-timeline-item {
  position: relative;
}

.el-timeline-item__content {
  margin-left: 20px;
}

.submission-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin: 10px 0;
}

.attachment-link {
  display: inline-flex;
  align-items: center;
  margin-top: 10px;
}

.attachment-link i {
  margin-right: 5px;
}

.status-tag {
  margin-left: 10px;
}

.deadline-info {
  color: #909399;
  font-size: 14px;
  margin: 10px 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.el-card {
  margin-bottom: 20px;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 7px;
}

.assignment-detail {
  margin-bottom: 20px;
}

.detail-content {
  padding: 0 20px;
}

.detail-content h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.deadline-info {
  display: flex;
  align-items: center;
  color: #606266;
  margin: 15px 0;
  font-size: 14px;
}

.deadline-info i {
  margin-right: 5px;
  color: #409EFF;
}

.time-remaining {
  margin-left: 10px;
  color: #67C23A;
}

.requirements {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

.requirements h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.requirements div {
  color: #606266;
  line-height: 1.6;
}

/* 状态标签样式 */
.el-tag {
  margin-left: 10px;
}

/* 提交历史样式优化 */
.history-card {
  margin-top: 20px;
}

.history-card .el-timeline {
  padding: 20px;
}

.history-card .el-timeline-item__content {
  color: #606266;
}

.history-card .el-timeline-item__timestamp {
  color: #909399;
  font-size: 13px;
}

/* 表单样式优化 */
.el-form-item__content {
  margin-left: 0 !important;
}

.upload-demo {
  margin-top: 15px;
}

.el-upload-list {
  margin-top: 10px;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
}

.form-actions .el-button {
  margin-left: 10px;
}
</style>
