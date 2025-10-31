<template>
  <div class="assignment-submissions">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ assignment?.title }} - 提交情况</span>
          <div class="header-buttons">
            <el-button type="primary" @click="exportToExcel">导出Excel</el-button>
            <el-button type="primary" @click="loadSubmissions">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 统计信息 -->
      <el-row :gutter="20" class="statistics">
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>总人数</span>
              </div>
            </template>
            <div class="stat-number">{{ totalStudents }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>已提交</span>
              </div>
            </template>
            <div class="stat-number success">{{ submittedCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>未提交</span>
              </div>
            </template>
            <div class="stat-number warning">{{ notSubmittedCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>逾期提交</span>
              </div>
            </template>
            <div class="stat-number danger">{{ lateSubmissionCount }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 提交列表 -->
      <el-table :data="submissions" style="width: 100%" v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="studentId" label="学号" />
        <el-table-column label="提交状态">
          <template #default="{ row }">
            <el-tag :type="getSubmissionStatusType(row)">
              {{ getSubmissionStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" />
        <el-table-column label="评分" width="200">
          <template #default="{ row }">
            <div v-if="row.status === 'GRADED'">
              <div>分数：{{ row.score }}分</div>
              <div v-if="row.comment" class="comment">评语：{{ row.comment }}</div>
            </div>
            <div v-else>-</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button v-if="row.status === 'SUBMITTED' || row.status === 'GRADED'"
              type="primary" size="small" @click="viewSubmission(row)">
              查看内容
            </el-button>
            <el-button v-if="row.attachmentUrl" type="success" size="small"
              @click="downloadFile(row)">
              下载附件
            </el-button>
            <el-button v-if="row.status === 'SUBMITTED'" type="warning" size="small"
              @click="openGradeDialog(row)">
              打分
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="submissions.length === 0" class="empty-text">
        暂无提交记录
      </div>
    </el-card>

    <!-- 查看提交内容对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`${currentSubmission?.studentName || ''} 的提交内容`"
      width="50%"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="提交时间">
          {{ currentSubmission?.submitTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="作业内容">
          {{ currentSubmission?.content }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentSubmission?.attachmentUrl" label="附件">
          <el-button type="primary" link @click="downloadFile(currentSubmission)">
            下载附件
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentSubmission?.score" label="分数">
          {{ currentSubmission?.score }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentSubmission?.comment" label="评语">
          {{ currentSubmission?.comment }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 打分对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      :title="`为 ${currentSubmission?.studentName || ''} 打分`"
      width="30%"
    >
      <el-form :model="gradeForm" label-width="80px">
        <el-form-item label="分数">
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input
            v-model="gradeForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGrade">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import * as XLSX from 'xlsx'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const dialogVisible = ref(false)
const gradeDialogVisible = ref(false)
const assignment = ref(null)
const submissions = ref([])
const currentSubmission = ref(null)
const gradeForm = ref({
  score: 0,
  comment: ''
})

// 统计数据
const totalStudents = computed(() => submissions.value.length)
const submittedCount = computed(() => submissions.value.filter(s => s.status === 'SUBMITTED' || s.status === 'GRADED').length)
const notSubmittedCount = computed(() => submissions.value.filter(s => !s.status || s.status !== 'SUBMITTED').length)
const lateSubmissionCount = computed(() => {
  if (!assignment.value) return 0
  const deadline = new Date(assignment.value.deadline)
  return submissions.value.filter(s => {
    if (!s.submitTime || (s.status !== 'SUBMITTED' && s.status !== 'GRADED')) return false
    return new Date(s.submitTime) > deadline
  }).length
})

// 加载作业信息
const loadAssignment = async () => {
  try {
    loading.value = true
    const { data } = await request.get(`/assignments/${route.params.id}`)
    if (data.code === 200) {
      assignment.value = data.data
    } else {
      ElMessage.error(data.message || '获取作业信息失败')
    }
  } catch (error) {
    console.error('获取作业信息失败:', error)
    ElMessage.error('获取作业信息失败')
  } finally {
    loading.value = false
  }
}

// 加载提交情况
const loadSubmissions = async () => {
  try {
    loading.value = true
    const { data } = await request.get(`/api/homework-submissions/homework/${route.params.id}`)
    if (data.code === 200) {
      submissions.value = data.data || []
    } else {
      throw new Error(data.message || '获取提交记录失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '获取提交记录失败')
  } finally {
    loading.value = false
  }
}

// 获取提交状态类型
const getSubmissionStatusType = (submission) => {
  if (submission.status === 'GRADED') return 'success'
  if (!submission.status || submission.status !== 'SUBMITTED') return 'warning'
  if (new Date(submission.submitTime) > new Date(assignment.value?.deadline)) return 'danger'
  return 'success'
}

// 获取提交状态文本
const getSubmissionStatusText = (submission) => {
  if (submission.status === 'GRADED') return '已评分'
  if (!submission.status || submission.status !== 'SUBMITTED') return '未提交'
  if (new Date(submission.submitTime) > new Date(assignment.value?.deadline)) return '逾期提交'
  return '已提交'
}

// 查看提交内容
const viewSubmission = (submission) => {
  currentSubmission.value = submission
  dialogVisible.value = true
}

// 下载附件
const downloadFile = (submission) => {
  if (!submission.attachmentUrl) return
  const url = submission.attachmentUrl.replace('/files/', '/uploads/')
  window.open(url, '_blank')
}

// 打开打分对话框
const openGradeDialog = (submission) => {
  if (submission.status === 'GRADED') {
    ElMessage.warning('该作业已评分')
    return
  }
  currentSubmission.value = submission
  gradeForm.value = {
    score: submission.score || 0,
    comment: submission.comment || ''
  }
  gradeDialogVisible.value = true
}

// 提交打分
const submitGrade = async () => {
  try {
    const { data } = await request.put(`/api/homework-submissions/${currentSubmission.value.id}/grade`, null, {
      params: {
        score: gradeForm.value.score,
        comment: gradeForm.value.comment
      }
    })
    if (data.code === 200) {
      ElMessage.success('打分成功')
      gradeDialogVisible.value = false
      await loadSubmissions() // 重新加载提交列表
    } else {
      throw new Error(data.message || '打分失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '打分失败')
  }
}

// 发送提醒
const sendReminder = async (submission) => {
  try {
    await request.post(`/api/notifications/homework-reminder`, {
      studentId: submission.studentId,
      assignmentId: route.params.id
    })
    ElMessage.success('提醒已发送')
  } catch (error) {
    ElMessage.error(error.message || '发送提醒失败')
  }
}

// 导出Excel
const exportToExcel = () => {
  const data = submissions.value.map(submission => ({
    '学生姓名': submission.studentName,
    '学号': submission.studentId,
    '提交状态': getSubmissionStatusText(submission),
    '提交时间': submission.submitTime || '-'
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '提交情况')
  
  const fileName = `${assignment.value?.title}-提交情况.xlsx`
  XLSX.writeFile(wb, fileName)
}

onMounted(async () => {
  await loadAssignment()
  await loadSubmissions()
})
</script>

<style scoped>
.assignment-submissions {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.statistics {
  margin-bottom: 20px;
}

.stat-header {
  text-align: center;
  font-weight: bold;
}

.stat-number {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-top: 10px;
}

.success {
  color: #67C23A;
}

.warning {
  color: #E6A23C;
}

.danger {
  color: #F56C6C;
}

.comment {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
