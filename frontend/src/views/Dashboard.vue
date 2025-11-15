<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <h2>欢迎使用课程资源平台</h2>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
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
        <el-card shadow="hover" class="stat-card">
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
        <el-card shadow="hover" class="stat-card">
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

    <!-- 数据可视化图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>课程教师分布</span>
            </div>
          </template>
          <div ref="courseDistributionChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>资源与作业月度统计</span>
            </div>
          </template>
          <div ref="monthlyStatsChart" class="chart-container"></div>
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
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import service from '@/utils/request'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

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

// 图表引用
const courseDistributionChart = ref(null)
const monthlyStatsChart = ref(null)
const courseChartInstance = ref(null)
const monthlyChartInstance = ref(null)

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

// 获取课程分布数据
const fetchCourseDistribution = async () => {
  try {
    const { data } = await service.get('/courses/all')
    if (data.code === 200 && data.data) {
      const courses = data.data
      // 按教师统计课程数量
      const teacherDistribution = {}
      courses.forEach(course => {
        const teacherName = course.teacherName || '未知教师'
        teacherDistribution[teacherName] = (teacherDistribution[teacherName] || 0) + 1
      })
      return teacherDistribution
    }
    return {}
  } catch (error) {
    console.error('获取课程分布失败:', error.response?.data || error)
    // 发生错误时返回空对象，不使用模拟数据
    return {}
  }
}

// 获取月度统计数据
const fetchMonthlyStats = async () => {
  try {
    // 只从后端获取真实数据
    const { data } = await service.get('/dashboard/monthly-stats')
    console.log('Monthly stats raw response:', data)
    if (data.code === 200 && data.data) {
      console.log('Using real monthly stats data:', data.data)
      return data.data
    }
    
    console.warn('No valid data received from backend')
    // 返回空数据结构，确保图表能正常初始化但不显示模拟数据
    return {
      months: [],
      resources: [],
      assignments: []
    }
  } catch (error) {
    console.error('获取月度统计失败:', error.response?.data || error)
    // 发生错误时返回空数据
    return {
      months: [],
      resources: [],
      assignments: []
    }
  }
}

// 初始化课程分布饼图
const initCourseDistributionChart = async () => {
  if (!courseDistributionChart.value) return
  
  // 销毁已有实例
  if (courseChartInstance.value) {
    courseChartInstance.value.dispose()
  }
  
  courseChartInstance.value = echarts.init(courseDistributionChart.value)
  const distribution = await fetchCourseDistribution()
  
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
        name: '课程分布',
        type: 'pie',
        radius: ['40%', '70%'],
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
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: Object.entries(distribution).map(([name, value]) => ({
          value,
          name
        }))
      }
    ],
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#C0C4CC']
  }
  
  courseChartInstance.value.setOption(option)
}

// 初始化月度统计柱状图
const initMonthlyStatsChart = async () => {
  if (!monthlyStatsChart.value) return
  
  // 销毁已有实例
  if (monthlyChartInstance.value) {
    monthlyChartInstance.value.dispose()
  }
  
  monthlyChartInstance.value = echarts.init(monthlyStatsChart.value)
  const statsData = await fetchMonthlyStats()
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['资源数', '作业数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: statsData.months
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '资源数',
        type: 'bar',
        data: statsData.resources,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '作业数',
        type: 'bar',
        data: statsData.assignments,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }
  
  monthlyChartInstance.value.setOption(option)
}

// 监听窗口大小变化，自适应图表
const handleResize = () => {
  if (courseChartInstance.value) {
    courseChartInstance.value.resize()
  }
  if (monthlyChartInstance.value) {
    monthlyChartInstance.value.resize()
  }
}

onMounted(() => {
  console.log('Dashboard mounted, fetching data...')
  console.log('Current auth token:', store.state.token)
  
  // 获取基础数据
  fetchStats()
  fetchRecentAssignments()
  fetchRecentResources()
  
  // 延迟初始化图表，确保DOM已经渲染
  setTimeout(() => {
    initCourseDistributionChart()
    initMonthlyStatsChart()
  }, 100)
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理资源
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (courseChartInstance.value) {
    courseChartInstance.value.dispose()
  }
  if (monthlyChartInstance.value) {
    monthlyChartInstance.value.dispose()
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.stat-cards {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

.card-content {
  text-align: center;
  padding: 30px 0;
  position: relative;
  overflow: hidden;
}

.card-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 194, 58, 0.1) 100%);
  z-index: 0;
}

.card-content > span {
  position: relative;
  z-index: 1;
}

.number {
  font-size: 48px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 8px;
  animation: fadeInUp 0.5s ease-out;
}

.label {
  color: #909399;
  font-size: 16px;
}

.list-content {
  min-height: 300px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

:deep(.el-timeline-item__content h4) {
  color: #303133;
  margin: 0;
  font-weight: 500;
}

:deep(.el-timeline-item__content p) {
  color: #909399;
  margin: 4px 0 0;
}

:deep(.el-timeline-item__timestamp) {
  color: #606266;
  font-size: 12px;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
  
  .number {
    font-size: 36px;
  }
  
  .card-content {
    padding: 20px 0;
  }
}
</style>
