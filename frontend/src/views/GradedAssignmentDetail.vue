<template>
  <div class="graded-assignment-detail">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>作业详情</span>
          <el-button type="primary" @click="handleBack">返回</el-button>
        </div>
      </template>

      <!-- 作业信息部分 -->
      <el-card class="info-card">
        <template #header>
          <span>作业信息</span>
        </template>
        <div class="info-item">
          <span class="label">作业标题：</span>
          <span class="value">{{ assignmentInfo.title }}</span>
        </div>
        <div class="info-item">
          <span class="label">作业描述：</span>
          <div class="value">{{ assignmentInfo.description }}</div>
        </div>
        <div class="info-item">
          <span class="label">截止日期：</span>
          <span class="value">{{ assignmentInfo.deadline ? new Date(assignmentInfo.deadline).toLocaleString() : '-' }}</span>
        </div>
      </el-card>

      <!-- 提交信息部分 -->
      <el-card class="info-card" style="margin-top: 20px;">
        <template #header>
          <span>提交信息</span>
        </template>
        <div class="info-item">
          <span class="label">提交时间：</span>
          <span class="value">{{ submissionInfo.submitTime ? new Date(submissionInfo.submitTime).toLocaleString() : '-' }}</span>
        </div>
        <div class="info-item" v-if="submissionInfo.content">
          <span class="label">提交内容：</span>
          <div class="value">{{ submissionInfo.content }}</div>
        </div>
        
        <!-- 附件下载部分 -->
        <div class="info-item" v-if="submissionInfo.attachmentUrl">
          <span class="label">提交附件：</span>
          <div class="file-list">
            <el-button
              type="primary"
              @click="handleDownload"
            >
              {{ submissionInfo.attachmentName || '附件' }}
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 评分信息部分 -->
      <el-card class="info-card" style="margin-top: 20px;">
        <template #header>
          <span>评分信息</span>
        </template>
        <div class="score-display">
          <div class="score-number">{{ submissionInfo.score || 0 }}</div>
          <div class="score-label">分</div>
        </div>
        <div class="info-item" v-if="submissionInfo.comment">
          <span class="label">教师评语：</span>
          <div class="comment-content">{{ submissionInfo.comment }}</div>
        </div>
        <div class="info-item">
          <span class="label">评分时间：</span>
          <span class="value">{{ submissionInfo.gradeTime ? new Date(submissionInfo.gradeTime).toLocaleString() : '-' }}</span>
        </div>
      </el-card>

      <!-- 其他同学分数部分 -->
      <el-card class="info-card" style="margin-top: 20px;">
        <template #header>
          <span>班级分数分布</span>
        </template>
        <div v-if="otherStudentsScores.length > 0" class="score-stats">
          <el-statistic title="平均分" :value="averageScore" precision="1" />
          <el-statistic title="最高分" :value="maxScore" />
          <el-statistic title="最低分" :value="minScore" />
          <el-statistic title="参与评分人数" :value="otherStudentsScores.length" />
        </div>
        <el-table :data="otherStudentsScores" style="width: 100%" v-if="otherStudentsScores.length > 0">
          <el-table-column prop="studentName" label="学生姓名" />
          <el-table-column prop="score" label="得分">
            <template #default="{ row }">
              <div :class="{ 'high-score': row.score >= 90, 'low-score': row.score < 60 }">
                {{ row.score }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="gradedTime" label="评分时间">
            <template #default="{ row }">
              {{ row.gradedTime ? new Date(row.gradedTime).toLocaleString() : '-' }}
            </template>
          </el-table-column>
        </el-table>
        <div v-if="otherStudentsScores.length === 0 && !loading" class="empty-text">
          暂无其他同学的评分数据
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup name="graded-assignment-detail">
import { ref, computed, onMounted } from 'vue'
import { ElStatistic } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useStore } from 'vuex'

const router = useRouter()
const route = useRoute()
const store = useStore()
const loading = ref(false)
const assignmentInfo = ref({})
const submissionInfo = ref({})
const otherStudentsScores = ref([])

// 计算平均分
const averageScore = computed(() => {
  if (otherStudentsScores.value.length === 0) return 0
  const sum = otherStudentsScores.value.reduce((acc, item) => acc + (item.score || 0), 0)
  return sum / otherStudentsScores.value.length
})

// 计算最高分
const maxScore = computed(() => {
  if (otherStudentsScores.value.length === 0) return 0
  return Math.max(...otherStudentsScores.value.map(item => item.score || 0))
})

// 计算最低分
const minScore = computed(() => {
  if (otherStudentsScores.value.length === 0) return 0
  return Math.min(...otherStudentsScores.value.map(item => item.score || 0))
})

// 获取作业ID
const assignmentId = computed(() => route.params.id)
// 获取当前用户ID
const userId = computed(() => store.state.user?.id || 0)

const loadAssignmentDetail = async () => {
  try {
    loading.value = true
    
    // 获取作业基本信息
    const assignmentResponse = await request.get(`/api/assignments/student/${userId.value}/assignment/${assignmentId.value}`)
    if (assignmentResponse.data && assignmentResponse.data.data) {
      assignmentInfo.value = assignmentResponse.data.data
    }
    
    // 获取当前学生的提交记录
    const submissionResponse = await request.get(`/api/homework-submissions/student/${userId.value}/assignment/${assignmentId.value}/latest`)
    if (submissionResponse.data && submissionResponse.data.data) {
      submissionInfo.value = submissionResponse.data.data
    }
    
    // 获取作业的所有提交记录（包含其他同学的分数）
    const submissionsResponse = await request.get(`/api/homework-submissions/homework/${assignmentId.value}`)
    if (submissionsResponse.data && submissionsResponse.data.data) {
      // 过滤出已评分的记录并排除当前学生自己的记录
      otherStudentsScores.value = submissionsResponse.data.data
        .filter(item => item.status === 'GRADED' && item.studentId !== userId.value)
        .map(item => ({
          studentName: item.studentName,
          score: item.score,
          gradedTime: item.gradeTime
        }))
    }
  } catch (error) {
    ElMessage.error('获取作业详情失败')
    console.error('获取作业详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleDownload = async () => {
      if (!submissionInfo.value.attachmentUrl) {
        ElMessage.warning('没有附件可下载')
        return
      }
      
      try {
        // 使用fetch API下载文件
        const response = await fetch(submissionInfo.value.attachmentUrl);
        if (!response.ok) {
          throw new Error('网络响应错误');
        }
        
        // 获取文件名
        let filename = submissionInfo.value.attachmentName;
        if (!filename) {
          // 从URL或响应头中提取文件名
          const contentDisposition = response.headers.get('content-disposition');
          if (contentDisposition) {
            const match = contentDisposition.match(/filename="(.+)"/);
            if (match && match[1]) {
              filename = match[1];
            }
          }
          if (!filename) {
            // 使用URL中的文件名作为默认值
            filename = submissionInfo.value.attachmentUrl.split('/').pop();
          }
        }
        
        // 将响应转换为Blob
        const blob = await response.blob();
        
        // 创建下载链接并触发点击
        const link = document.createElement('a');
        const url = window.URL.createObjectURL(blob);
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        
        // 清理
        window.URL.revokeObjectURL(url);
        document.body.removeChild(link);
      } catch (error) {
        console.error('下载文件失败:', error);
        ElMessage.error('文件下载失败，请稍后重试');
      }
    }

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadAssignmentDetail()
})
</script>

<style scoped>
.graded-assignment-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-card {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}

.value {
  color: #303133;
  line-height: 1.6;
}

.file-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}

.score-display {
  display: flex;
  align-items: baseline;
  margin-bottom: 20px;
}

.score-number {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
  margin-right: 10px;
}

.score-label {
  font-size: 24px;
  color: #606266;
}

.comment-content {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-top: 8px;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.score-stats {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.score-stats :deep(.el-statistic) {
  margin: 0 10px 10px 0;
}

.high-score {
  color: #67c23a;
  font-weight: bold;
}

.low-score {
  color: #f56c6c;
}

:deep(.el-table__body tr:hover > td) {
  background-color: #fafafa;
}
</style>