<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <h2>欢迎使用课程资源平台</h2>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>课程总数</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ stats.courses }}</span>
            <span class="label">个课程</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>资源总数</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ stats.resources }}</span>
            <span class="label">个资源</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>作业总数</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ stats.assignments }}</span>
            <span class="label">个作业</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="recent-section">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近作业</span>
            </div>
          </template>
          <div class="list-content">
            <el-empty v-if="!recentAssignments.length" description="暂无作业" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="item in recentAssignments"
                :key="item.id"
                :timestamp="item.deadline"
                placement="top"
              >
                <h4>{{ item.title }}</h4>
                <p>{{ item.courseName }}</p>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最新资源</span>
            </div>
          </template>
          <div class="list-content">
            <el-empty v-if="!recentResources.length" description="暂无资源" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="item in recentResources"
                :key="item.id"
                :timestamp="item.createTime"
                placement="top"
              >
                <h4>{{ item.name }}</h4>
                <p>{{ item.courseName }}</p>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import service from '@/utils/request'

const store = useStore()

// 统计数据
const stats = ref({
  courses: 0,
  resources: 0,
  assignments: 0
})

// 最近作业
const recentAssignments = ref([])

// 最新资源
const recentResources = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    console.log('Fetching stats...')
    const { data } = await service.get('/stats')
    console.log('Stats raw response:', data)
    if (data.code === 200) {
      stats.value = data.data
      console.log('Updated stats:', stats.value)
    } else {
      console.error('Stats request failed:', data)
    }
  } catch (error) {
    console.error('Error fetching stats:', error.response?.data || error)
  }
}

// 获取最近作业
const fetchRecentAssignments = async () => {
  try {
    console.log('Fetching recent assignments...')
    const { data } = await service.get('/dashboard/recent-assignments')
    console.log('Assignments raw response:', data)
    if (data.code === 200) {
      recentAssignments.value = data.data
      console.log('Updated assignments:', recentAssignments.value)
    } else {
      console.error('Assignments request failed:', data)
    }
  } catch (error) {
    console.error('Error fetching recent assignments:', error.response?.data || error)
  }
}

// 获取最新资源
const fetchRecentResources = async () => {
  try {
    console.log('Fetching recent resources...')
    const { data } = await service.get('/resources/recent')
    console.log('Resources raw response:', data)
    if (data.code === 200) {
      recentResources.value = data.data
      console.log('Updated resources:', recentResources.value)
    } else {
      console.error('Resources request failed:', data)
    }
  } catch (error) {
    console.error('Error fetching recent resources:', error.response?.data || error)
  }
}

onMounted(() => {
  console.log('Dashboard mounted, fetching data...')
  console.log('Current auth token:', store.state.token)
  fetchStats()
  fetchRecentAssignments()
  fetchRecentResources()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #303133;
}

.stat-cards {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 8px;
}

.label {
  color: #909399;
}

.list-content {
  min-height: 300px;
}

:deep(.el-timeline-item__content h4) {
  color: #303133;
  margin: 0;
}

:deep(.el-timeline-item__content p) {
  color: #909399;
  margin: 4px 0 0;
}

:deep(.el-timeline-item__timestamp) {
  color: #606266;
}
</style>
