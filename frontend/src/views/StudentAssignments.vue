<template>
  <div class="student-assignments">
    <!-- 图表统计卡片 -->
    <el-card class="stats-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>作业评分统计</span>
        </div>
      </template>
      <div class="stats-container">
        <div class="chart-container">
          <div id="scoreDistributionChart" ref="chartRef" class="chart"></div>
        </div>
        <div class="avg-score-container">
          <div class="avg-score-item">
            <span class="avg-score-label">平均分：</span>
            <span class="avg-score-value">{{ averageScore || '-' }}</span>
          </div>
          <div class="score-range-item">
            <span class="range-label">0-60分：</span>
            <span class="range-value">{{ scoreDistribution.range0to60 }}份</span>
          </div>
          <div class="score-range-item">
            <span class="range-label">60-80分：</span>
            <span class="range-value">{{ scoreDistribution.range60to80 }}份</span>
          </div>
          <div class="score-range-item">
            <span class="range-label">80-100分：</span>
            <span class="range-value">{{ scoreDistribution.range80to100 }}份</span>
          </div>
        </div>
      </div>
    </el-card>

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
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import request from '@/utils/request'

const router = useRouter()
const store = useStore()
const loading = ref(false)
const assignments = ref([])
const chartRef = ref(null)
const chartInstance = ref(null)
const scoreDistribution = ref({
  range0to60: 0,
  range60to80: 0,
  range80to100: 0
})
const averageScore = ref(null)

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

// 计算评分分布
const calculateScoreDistribution = () => {
  const gradedAssignments = assignments.value.filter(assign => 
    assign.submissionStatus === 'GRADED' && assign.score !== undefined
  )
  
  const distribution = {
    range0to60: 0,
    range60to80: 0,
    range80to100: 0
  }
  
  let totalScore = 0
  let scoredCount = 0
  
  gradedAssignments.forEach(assign => {
    const score = Number(assign.score)
    if (!isNaN(score)) {
      scoredCount++
      totalScore += score
      
      if (score >= 0 && score < 60) {
        distribution.range0to60++
      } else if (score >= 60 && score < 80) {
        distribution.range60to80++
      } else if (score >= 80 && score <= 100) {
        distribution.range80to100++
      }
    }
  })
  
  scoreDistribution.value = distribution
  
  // 计算平均分
  if (scoredCount > 0) {
    averageScore.value = (totalScore / scoredCount).toFixed(1)
  } else {
    averageScore.value = null
  }
  
  return distribution
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
  
  chartInstance.value = echarts.init(chartRef.value)
  
  updateChart()
}

// 更新图表数据
const updateChart = () => {
  if (!chartInstance.value) return
  
  const distribution = calculateScoreDistribution()
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 份 ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: '0%'
    },
    color: ['#F56C6C', '#E6A23C', '#67C23A'],
    series: [
      {
        name: '作业评分分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}份'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: [
          { value: distribution.range0to60, name: '0-60分' },
          { value: distribution.range60to80, name: '60-80分' },
          { value: distribution.range80to100, name: '80-100分' }
        ]
      }
    ]
  }
  
  chartInstance.value.setOption(option)
}

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
    
    // 数据加载完成后更新图表
    await nextTick()
    updateChart()
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

onMounted(async () => {
  await loadAssignments()
  await nextTick()
  initChart()
  
  // 监听窗口大小变化，重绘图表
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理资源
onUnmounted(() => {
  cleanup()
})

// 窗口大小变化处理函数
const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

// 组件卸载时销毁图表实例和移除事件监听
const cleanup = () => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  window.removeEventListener('resize', handleResize)
}
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

/* 统计卡片样式 */
.stats-card {
  margin-bottom: 20px;
}

.stats-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 10px 0;
}

.chart-container {
  flex: 1;
  min-width: 300px;
  height: 300px;
}

.chart {
  width: 100%;
  height: 100%;
}

.avg-score-container {
  flex: 1;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
}

.avg-score-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.avg-score-label {
  font-size: 16px;
  color: #606266;
}

.avg-score-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.score-range-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 4px;
}

.range-label {
  font-size: 14px;
  color: #606266;
}

.range-value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

/* 作业列表样式 */
.box-card {
  margin-bottom: 20px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-container {
    flex-direction: column;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .avg-score-value {
    font-size: 20px;
  }
}
</style>
