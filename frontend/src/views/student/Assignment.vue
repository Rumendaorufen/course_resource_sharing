<template>
  <div class="student-assignment">
    <div class="header">
      <h2>我的作业</h2>
      <el-button type="primary" @click="refreshList">刷新</el-button>
    </div>

    <el-table :data="assignments" border style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="作业标题" />
      <el-table-column prop="courseName" label="所属课程" />
      <el-table-column prop="teacherName" label="教师" />
      <el-table-column prop="deadline" label="截止日期">
        <template #default="scope">
          {{ formatDate(scope.row.deadline) }}
        </template>
      </el-table-column>
      <el-table-column prop="submissionStatus" label="提交状态">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.submissionStatus)">
            {{ getStatusText(scope.row.submissionStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分数">
        <template #default="scope">
          {{ scope.row.score || '未批改' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button
            size="small"
            type="primary"
            @click="viewAssignment(scope.row)"
            :disabled="isDeadlinePassed(scope.row.deadline)"
          >
            查看
          </el-button>
          <el-button
            size="small"
            type="success"
            @click="submitAssignment(scope.row)"
            :disabled="isDeadlinePassed(scope.row.deadline) || scope.row.submissionStatus === 'GRADED'"
          >
            提交
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 提交作业对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="提交作业"
      width="50%"
    >
      <el-form :model="submissionForm" label-width="80px">
        <el-form-item label="作业内容">
          <el-input
            type="textarea"
            v-model="submissionForm.content"
            :rows="4"
            placeholder="请输入作业内容"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            :action="`/api/assignments/student/${submissionForm.assignmentId}/upload`"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确认提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { formatDate } from '@/utils/date'

export default {
  name: 'StudentAssignment',
  setup() {
    const store = useStore()
    const loading = ref(false)
    const assignments = ref([])
    const dialogVisible = ref(false)
    const submissionForm = ref({
      assignmentId: null,
      content: '',
      attachmentUrl: ''
    })

    // 获取作业列表
    const getAssignments = async () => {
      loading.value = true
      try {
        const userId = store.state.user.id
        const { data } = await request.get(`/api/assignments/student/${userId}`)
        assignments.value = data
      } catch (error) {
        ElMessage.error('获取作业列表失败')
        console.error(error)
      } finally {
        loading.value = false
      }
    }

    // 刷新列表
    const refreshList = () => {
      getAssignments()
    }

    // 获取状态样式
    const getStatusType = (status) => {
      const statusMap = {
        'NOT_SUBMITTED': 'warning',
        'SUBMITTED': 'primary',
        'GRADED': 'success',
        'LATE': 'danger'
      }
      return statusMap[status] || 'info'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'NOT_SUBMITTED': '未提交',
        'SUBMITTED': '已提交',
        'GRADED': '已批改',
        'LATE': '逾期提交'
      }
      return statusMap[status] || status
    }

    // 检查是否过期
    const isDeadlinePassed = (deadline) => {
      return new Date(deadline) < new Date()
    }

    // 查看作业
    const viewAssignment = async (assignment) => {
      try {
        const userId = store.state.user.id
        const { data } = await request.get(`/api/assignments/student/${userId}/assignment/${assignment.id}`)
        ElMessageBox.alert(data.description, assignment.title, {
          confirmButtonText: '确定',
          callback: () => {}
        })
      } catch (error) {
        ElMessage.error('获取作业详情失败')
        console.error(error)
      }
    }

    // 打开提交对话框
    const submitAssignment = (assignment) => {
      submissionForm.value.assignmentId = assignment.id
      dialogVisible.value = true
    }

    // 处理文件上传成功
    const handleUploadSuccess = (response) => {
      submissionForm.value.attachmentUrl = response.data
      ElMessage.success('文件上传成功')
    }

    // 处理文件上传失败
    const handleUploadError = () => {
      ElMessage.error('文件上传失败')
    }

    // 提交作业
    const handleSubmit = async () => {
      try {
        await request.post(`/api/assignments/student/${submissionForm.value.assignmentId}/submit`, submissionForm.value)
        ElMessage.success('作业提交成功')
        dialogVisible.value = false
        getAssignments() // 刷新列表
      } catch (error) {
        ElMessage.error('作业提交失败')
        console.error(error)
      }
    }

    onMounted(() => {
      getAssignments()
    })

    return {
      loading,
      assignments,
      dialogVisible,
      submissionForm,
      refreshList,
      formatDate,
      getStatusType,
      getStatusText,
      isDeadlinePassed,
      viewAssignment,
      submitAssignment,
      handleUploadSuccess,
      handleUploadError,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.student-assignment {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-footer {
  margin-top: 20px;
}

.upload-demo {
  margin-top: 10px;
}
</style>
