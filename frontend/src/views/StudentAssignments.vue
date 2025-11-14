<template>
  <div class="student-assignments">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>我的作业</span>
          <el-button type="primary" @click="loadAssignments">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="paginatedAssignments" style="width: 100%">
        <el-table-column prop="title" label="作业标题" />
        <el-table-column prop="description" label="作业描述" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止日期">
          <template #default="{ row }">
            {{ new Date(row.deadline).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType" size="small">
              {{ row.statusText }}
            </el-tag>
            <div v-if="row.submissionTime" class="submission-time">
              提交时间：{{ new Date(row.submissionTime).toLocaleString() }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分">
          <template #default="{ row }">
            <el-tag v-if="row.submissionStatus === 'GRADED'" type="success">
              {{ row.score }}分
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <template v-if="row.submissionStatus === 'GRADED' || row.submissionStatus === '已评分'">
              <el-button
                type="success"
                size="small"
                @click="handleViewGradedAssignment(row.id)"
              >
                查看详情
              </el-button>
            </template>
            <template v-else>
              <el-button
                v-if="row.submissionStatus === 'NOT_SUBMITTED' || row.submissionStatus === '未提交'"
                type="primary"
                size="small"
                @click="handleSubmit(row.id)"
                :disabled="isDeadlinePassed(row.deadline)"
              >
                提交作业
              </el-button>
              <template v-else-if="row.submissionStatus === 'SUBMITTED' || row.submissionStatus === '已提交'">
                <el-button
                  type="info"
                  size="small"
                  @click="handleView(row.id)"
                >
                  查看/修改
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteSubmission(row)"
                  :disabled="isDeadlinePassed(row.deadline)"
                >
                  删除提交
                </el-button>
              </template>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="assignments.length === 0" class="empty-text">
        暂无作业
      </div>

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
    </el-card>
  </div>
</template>

<script setup name="student-assignments">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const store = useStore()
const loading = ref(false)
const assignments = ref([])

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

const loadAssignments = async () => {
  try {
    loading.value = true
    const { data } = await request.get(`/api/assignments/student/${store.state.user.id}`)
    assignments.value = data.data.map(assignment => {
      return {
        ...assignment,
        submissionStatus: assignment.submissionStatus || 'NOT_SUBMITTED',
        statusType: getStatusType(assignment),
        statusText: getStatusText(assignment)
      }
    })
    total.value = assignments.value.length
  } catch (error) {
    ElMessage.error('获取作业列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const getStatusType = (assignment) => {
  if (assignment.submissionStatus === 'GRADED' || assignment.submissionStatus === '已评分') return 'success'
  if (assignment.submissionStatus === 'SUBMITTED' || assignment.submissionStatus === '已提交') {
    return new Date() > new Date(assignment.deadline) ? 'warning' : 'info'
  }
  return new Date() > new Date(assignment.deadline) ? 'danger' : ''
}

const getStatusText = (assignment) => {
  if (assignment.submissionStatus === 'GRADED' || assignment.submissionStatus === '已评分') return '已评分'
  if (assignment.submissionStatus === 'SUBMITTED' || assignment.submissionStatus === '已提交') {
    return new Date() > new Date(assignment.deadline) ? '已截止' : '已提交'
  }
  return new Date() > new Date(assignment.deadline) ? '已截止' : '未提交'
}

const isDeadlinePassed = (deadline) => {
  return new Date() > new Date(deadline)
}

const handleSubmit = (assignmentId) => {
  router.push({
    name: 'HomeworkSubmission',
    params: { id: assignmentId, mode: 'submit' }
  })
}

const handleView = (assignmentId) => {
  router.push({
    name: 'HomeworkSubmission',
    params: { id: assignmentId, mode: 'view' }
  })
}

const handleViewGradedAssignment = (assignmentId) => {
  router.push({
    name: 'GradedAssignmentDetail',
    params: { id: assignmentId }
  })
}

const handleDeleteSubmission = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该作业提交吗？此操作不可撤销！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 首先获取学生对该作业的最新提交记录
    const latestResponse = await request.get(
      `/api/homework-submissions/student/${store.state.user.id}/assignment/${row.id}/latest`
    )
    
    if (latestResponse.data.code !== 200 || !latestResponse.data.data) {
      ElMessage.error('未找到提交记录')
      return
    }
    
    const submissionId = latestResponse.data.data.id
    
    // 然后调用删除API
    const deleteResponse = await request.delete(`/api/homework-submissions/${submissionId}`)
    
    if (deleteResponse.data.code === 200) {
      ElMessage.success('删除提交成功')
      // 重新加载作业列表
      loadAssignments()
    } else {
      ElMessage.error(deleteResponse.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除提交失败:', error)
      ElMessage.error('删除提交失败，请稍后重试')
    }
  }
}

onMounted(() => {
  loadAssignments()
})
</script>

<style scoped>
.student-assignments {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.submission-time {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.score {
  margin-bottom: 4px;
}

.comment {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.graded-text {
  color: #67c23a;
  font-size: 13px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
