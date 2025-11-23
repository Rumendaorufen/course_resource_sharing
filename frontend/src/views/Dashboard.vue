<template>
  <div class="dashboard-container">
    <!-- 页面头部 -->
    <el-row :gutter="20" class="header-section">
      <el-col :span="24">
        <div class="header-content">
          <h2 class="page-title">欢迎使用课程资源平台</h2>
          <el-button type="primary" @click="refreshData" :loading="loading" size="small" class="refresh-btn">
            <el-icon :size="16"><i-ep-refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </el-col>
    </el-row>
    
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card card-enhanced fade-in" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><i-ep-book /></el-icon>
              <span class="header-title">课程总数</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ stats.courses }}</span>
            <span class="label">个课程</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card card-enhanced fade-in" shadow="hover" :style="{animationDelay: '0.1s'}">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><i-ep-document /></el-icon>
              <span class="header-title">资源总数</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ stats.resources }}</span>
            <span class="label">个资源</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card card-enhanced fade-in" shadow="hover" :style="{animationDelay: '0.2s'}">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><i-ep-document-checked /></el-icon>
              <span class="header-title">作业总数</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ stats.assignments }}</span>
            <span class="label">个作业</span>
          </div>
        </el-card>
      </el-col>
      
      <!-- 用户总数卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card card-enhanced fade-in" shadow="hover" :style="{animationDelay: '0.3s'}">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><i-ep-user /></el-icon>
              <span class="header-title">用户总数</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ stats.users }}</span>
            <span class="label">个用户</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域已移除 -->

    <!-- 资源统计图表区域已移除 -->
    <el-row :gutter="20" class="recent-section">

    </el-row>

    <!-- 最近活动区域 -->
    <el-row :gutter="20" class="recent-section">
      <el-col :xs="24" :md="12">
        <el-card class="activity-card card-enhanced fade-in" shadow="hover" :style="{animationDelay: '0.5s'}">
          <template #header>
            <div class="card-header flex-between">
              <div>
                <el-icon class="header-icon"><Timer /></el-icon>
                <span class="header-title">最近作业</span>
              </div>
              <router-link to="/assignments" class="more-link">
                查看全部
                <el-icon :size="14"><ArrowRight /></el-icon>
              </router-link>
            </div>
          </template>
          <div class="list-content">
            <el-empty v-if="!recentAssignments.length" description="暂无作业" />
            <el-timeline v-else class="assignment-timeline">
              <el-timeline-item
                v-for="(item, index) in recentAssignments"
                :key="item.id"
                :timestamp="formatDate(item.deadline)"
                placement="top"
                :type="getDeadlineType(item.deadline)"
                :icon="index === 0 ? (getDeadlineType(item.deadline) === 'danger' ? 'Warning' : 'DocumentChecked') : ''"
              >
                <el-card class="timeline-content card-enhanced">
                  <h4 class="timeline-title">{{ item.title }}</h4>
                  <p class="timeline-subtitle">{{ item.courseName }}</p>
                  <div class="timeline-meta">
                    <span class="deadline-label" :class="getDeadlineType(item.deadline)">
                      截止日期: {{ formatDateTime(item.deadline) }}
                    </span>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <el-card class="activity-card card-enhanced fade-in" shadow="hover" :style="{animationDelay: '0.6s'}">
          <template #header>
            <div class="card-header flex-between">
              <div>
                <el-icon class="header-icon"><StarFilled /></el-icon>
                <span class="header-title">最新资源</span>
              </div>
              <router-link to="/resources" class="more-link">
                查看全部
                <el-icon :size="14"><ArrowRight /></el-icon>
              </router-link>
            </div>
          </template>
          <div class="list-content">
            <el-empty v-if="!recentResources.length" description="暂无资源" />
            <el-timeline v-else class="resource-timeline">
              <el-timeline-item
                v-for="(item, index) in recentResources"
                :key="item.id"
                :timestamp="formatDate(item.createTime)"
                placement="top"
                type="success"
                :icon="index === 0 ? 'DocumentAdd' : ''"
              >
                <el-card class="timeline-content card-enhanced">
                  <h4 class="timeline-title">{{ item.name }}</h4>
                  <p class="timeline-subtitle">{{ item.courseName }}</p>
                  <div class="timeline-meta">
                    <span class="resource-type">{{ getItemType(item.type) }}</span>
                    <span class="resource-size" v-if="item.size">
                      {{ formatFileSize(item.size) }}
                    </span>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ChartComponent from '@/components/ChartComponent.vue'

import analyticsApi from '@/api/analytics'
// Element Plus图标组件不需要单独导入，直接使用i-ep-*格式

const store = useStore()
const router = useRouter()

// 统计数据
const stats = ref({
  courses: 0,
  resources: 0,
  assignments: 0,
  users: 0 // 新增用户总数
})

// 最近作业
const recentAssignments = ref([])

// 最新资源
const recentResources = ref([])

// 加载状态
const loading = ref(false)

// 图表加载状态
const chartLoading = ref({
  monthlyStats: false,
  learningTrend: false
})

// 图表错误状态
const chartError = ref({
  monthlyStats: null,
  learningTrend: null
})

// 月度统计数据
const monthlyStatsData = ref({
  months: [],
  resources: [],
  assignments: []
})



// 图表引用
const monthlyStatsChart = ref(null)
const monthlyChartInstance = ref(null)

// 计算当前主题
const isDarkTheme = computed(() => {
  return document.documentElement.classList.contains('dark')
})

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 日期时间格式化
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取作业截止类型
const getDeadlineType = (deadline) => {
  if (!deadline) return 'primary'
  const now = new Date()
  const deadlineDate = new Date(deadline)
  const diffDays = (deadlineDate - now) / (1000 * 60 * 60 * 24)
  
  if (diffDays < 0) return 'danger' // 已过期
  if (diffDays <= 3) return 'warning' // 3天内
  return 'success' // 正常
}

// 获取资源类型名称
const getItemType = (type) => {
  const typeMap = {
    'document': '文档',
    'video': '视频',
    'audio': '音频',
    'image': '图片',
    'other': '其他'
  }
  return typeMap[type] || type
}

// 刷新所有数据
const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchStats(),
      fetchRecentAssignments(),
      fetchRecentResources(),
      fetchUsersCount() // 添加获取用户总数
    ])
    
    // 重新初始化图表
    await initMonthlyStatsChart()
    
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
    console.error('Error refreshing data:', error)
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    console.log('Fetching dashboard stats...');
    const response = await analyticsApi.getDashboardStats()
    // 正确获取数据，因为后端返回格式为 {code, message, data}
    // 合并新的统计数据，而不是直接覆盖整个对象
    Object.assign(stats.value, response.data?.data || {})
  } catch (error) {
    console.error('Error fetching stats:', error.response?.data || error)
    // 发生错误时，只重置与统计数据相关的部分，不影响用户总数
    Object.assign(stats.value, { courses: 0, resources: 0, assignments: 0 })
  }
}

// 获取用户总数
const fetchUsersCount = async () => {
  try {
    console.log('Fetching users count...');
    const response = await analyticsApi.getUsersCount();
    let userCount = response.data?.data || 0;
    // 假设admin用户总是存在且只有一个，从总数中排除
    if (userCount > 0) {
      userCount--;
    }
    stats.value.users = userCount;
  } catch (error) {
    console.error('Error fetching users count:', error.response?.data || error);
    stats.value.users = 0;
  }
}

// 获取最近作业
const fetchRecentAssignments = async () => {
  try {
    console.log('Fetching recent assignments...');
    const response = await analyticsApi.getRecentAssignments()
    // 正确获取数据，按截止日期排序并限制只显示最近5个
    const assignments = response.data?.data || []
    // 按截止日期升序排序（距离现在最近的在前）
    assignments.sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
    // 限制只显示前5个
    recentAssignments.value = assignments.slice(0, 5)
  } catch (error) {
    console.error('Error fetching recent assignments:', error.response?.data || error)
    recentAssignments.value = []
  }
}

// 获取最新资源
const fetchRecentResources = async () => {
  try {
    console.log('Fetching recent resources...');
    const response = await analyticsApi.getRecentResources()
    // 正确获取数据并限制只显示最近5条
    recentResources.value = (response.data?.data || []).slice(0, 5)
  } catch (error) {
    console.error('Error fetching recent resources:', error.response?.data || error)
    recentResources.value = []
  }
}



// 获取月度统计数据
const fetchMonthlyStats = async () => {
  try {
    chartLoading.value.monthlyStats = true
    chartError.value.monthlyStats = null
    // 使用analytics服务获取数据
    const response = await analyticsApi.getMonthlyStats()
    // 检查是否有有效的数据，即使code不是200（可能是使用模拟数据）
    if (response.data && response.data.months) {
      const data = response.data
      monthlyStatsData.value = data
      // 如果是使用模拟数据，显示提示信息
      if (response.code !== 200) {
        ElMessage.warning(response.message || '当前显示的是模拟数据')
      }
      return data
    } else {
      ElMessage.error(response.message || '获取月度统计数据失败')
      // 使用默认模拟数据
      const mockData = {
        months: ['1月', '2月', '3月', '4月', '5月', '6月'],
        resources: [15, 23, 18, 32, 28, 45],
        assignments: [10, 15, 12, 20, 18, 25]
      }
      monthlyStatsData.value = mockData
      return mockData
    }
  } catch (error) {
    console.error('获取月度统计失败:', error.response?.data || error)
    chartError.value.monthlyStats = error.message
    ElMessage.error('获取月度统计数据时发生错误')
    // 使用默认模拟数据
    const mockData = {
      months: ['1月', '2月', '3月', '4月', '5月', '6月'],
      resources: [15, 23, 18, 32, 28, 45],
      assignments: [10, 15, 12, 20, 18, 25]
    }
    monthlyStatsData.value = mockData
    return mockData
  } finally {
    chartLoading.value.monthlyStats = false
  }
}

// 获取学习趋势数据
const fetchLearningTrend = async () => {
  try {
    chartLoading.value.learningTrend = true
    chartError.value.learningTrend = null
    const response = await analyticsApi.getLearningTrend()
    // 检查是否有有效的数据，即使code不是200（可能是使用模拟数据）
    if (response.data && response.data.dates && response.data.trendData) {
      // 如果是使用模拟数据，显示提示信息
      if (response.code !== 200) {
        ElMessage.warning(response.message || '当前显示的是模拟数据')
      }
      return response.data
    } else if (response.data) {
      return response.data
    } else {
      ElMessage.error(response.message || '获取学习趋势数据失败')
      // 使用默认模拟数据
      const dates = Array.from({length: 7}, (_, i) => {
        const d = new Date()
        d.setDate(d.getDate() - 6 + i)
        return `${d.getMonth() + 1}/${d.getDate()}`
      })
      return {
        dates: dates,
        trendData: [45, 48, 52, 47, 55, 62, 58]
      }
    }
  } catch (error) {
    console.error('获取学习趋势失败:', error.response?.data || error)
    chartError.value.learningTrend = error.message
    ElMessage.error('获取学习趋势数据时发生错误')
    // 使用默认模拟数据
    const dates = Array.from({length: 7}, (_, i) => {
      const d = new Date()
      d.setDate(d.getDate() - 6 + i)
      return `${d.getMonth() + 1}/${d.getDate()}`
    })
    return {
      dates: dates,
      trendData: [45, 48, 52, 47, 55, 62, 58]
    }
  } finally {
    chartLoading.value.learningTrend = false
  }
}

// 获取主题相关的图表颜色配置
const getChartThemeConfig = () => {
  const baseColors = {
    primary: 'var(--primary-color)',
    success: 'var(--success-color)',
    warning: 'var(--warning-color)',
    danger: 'var(--danger-color)',
    info: 'var(--info-color)'
  }
  
  const colorPalette = isDarkTheme.value 
    ? ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#C0C4CC', '#909399', '#67C23A', '#409EFF']
    : ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#C0C4CC', '#909399', '#67C23A', '#409EFF']
    
  const backgroundColor = getComputedStyle(document.documentElement).getPropertyValue('--background-color').trim()
  const textColor = getComputedStyle(document.documentElement).getPropertyValue('--text-color-primary').trim()
  const axisColor = getComputedStyle(document.documentElement).getPropertyValue('--border-color').trim()
  
  return {
    baseColors,
    colorPalette,
    backgroundColor,
    textColor,
    axisColor
  }
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
  const themeConfig = getChartThemeConfig()
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(0, 0, 0, 0.1)'
        }
      },
      backgroundColor: getComputedStyle(document.documentElement).getPropertyValue('--tooltip-background').trim(),
      textStyle: {
        color: getComputedStyle(document.documentElement).getPropertyValue('--tooltip-text-color').trim()
      },
      borderColor: getComputedStyle(document.documentElement).getPropertyValue('--border-color').trim()
    },
    legend: {
      data: ['资源数', '作业数'],
      textStyle: {
        color: themeConfig.textColor
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: statsData.months,
      axisLine: {
        lineStyle: {
          color: themeConfig.axisColor
        }
      },
      axisLabel: {
        color: themeConfig.textColor
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: themeConfig.axisColor
        }
      },
      axisLabel: {
        color: themeConfig.textColor
      },
      splitLine: {
        lineStyle: {
          color: themeConfig.axisColor,
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '资源数',
        type: 'bar',
        data: statsData.resources,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: themeConfig.baseColors.primary },
            { offset: 1, color: themeConfig.baseColors.primary + '80' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        animationDelay: function (idx) {
          return idx * 100;
        }
      },
      {
        name: '作业数',
        type: 'bar',
        data: statsData.assignments,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: themeConfig.baseColors.success },
            { offset: 1, color: themeConfig.baseColors.success + '80' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        animationDelay: function (idx) {
          return idx * 100 + 100;
        }
      }
    ],
    animationEasing: 'elasticOut',
    animationDelayUpdate: function (idx) {
      return idx * 5;
    }
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

// 监听主题变化，重新配置图表
const handleThemeChange = () => {
  // 重新初始化图表以适应新主题
  setTimeout(() => {

    initMonthlyStatsChart()
  }, 100)
}

onMounted(async () => {
  console.log('Dashboard onMounted hook executed.');
  // 获取基础数据
  await Promise.all([
    fetchStats(),
    fetchRecentAssignments(),
    fetchRecentResources(),
    fetchUsersCount() // 添加获取用户总数
  ])
  
  // 延迟初始化图表，确保DOM已经渲染
  setTimeout(() => {
    initCourseDistributionChart()
    initMonthlyStatsChart()
  }, 100)
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
  
  // 添加主题变化监听
  window.addEventListener('themeChanged', handleThemeChange)
})

// 监听主题变化
watch(isDarkTheme, () => {
  handleThemeChange()
})

// 获取学习趋势图表配置
const getLearningTrendConfig = () => {
  return {
    chartType: 'line',
    chartData: {
      xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      series: [{
        name: '学习时长',
        data: [30, 45, 35, 55, 40, 65, 50]
      }]
    }
  }
}



// 获取课程分布图表配置 - 简化配置，确保数据格式正确
const getCourseDistributionConfig = () => {
  console.log('返回课程分布图表配置，数据:', courseDistributionData.value)
  return {
    chartType: 'pie',
    chartData: {
      title: '课程分布',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      // ChartComponent需要直接使用数据数组
      data: courseDistributionData.value
    },
    legendPosition: 'left',
    showLegend: true
  }
}

// 获取月度统计图表配置
const getMonthlyStatsConfig = () => {
  // 使用响应式数据，如果没有数据则使用默认值
  const data = monthlyStatsData.value || {
    months: ['1月', '2月', '3月', '4月', '5月', '6月'],
    resources: [15, 23, 18, 32, 28, 45],
    assignments: [10, 15, 12, 20, 18, 25]
  }
  
  return {
    chartType: 'bar',
    chartData: {
      xAxis: data.months,
      series: [
        {
          name: '资源数',
          data: data.resources
        },
        {
          name: '作业数',
          data: data.assignments
        }
      ]
    }
  }
}

// 组件卸载时清理资源
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('themeChanged', handleThemeChange)
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
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.header-section {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  margin: 0;
  color: #409eff;
  font-size: 24px;
  font-weight: 600;
  font-family: inherit;
}

/* 统计卡片区域 */
.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  transition: all var(--transition-base);
}

.stat-card .card-header {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: var(--text-color-primary);
}

.header-icon {
  margin-right: 8px;
  color: var(--primary-color);
  font-size: 18px;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
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
  color: var(--primary-color);
  margin-right: 8px;
  animation: fadeInUp 0.5s ease-out;
}

.label {
  color: var(--text-color-secondary);
  font-size: 16px;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 350px;
  transition: all var(--transition-base);
}

/* 活动区域 */
.recent-section {
  margin-bottom: 24px;
}

.activity-card {
  height: 100%;
}

.list-content {
  min-height: 300px;
}

/* 时间线样式 */
.assignment-timeline,
.resource-timeline {
  margin-left: 10px;
}

.timeline-content {
  margin-top: 8px;
  transition: all var(--transition-base);
}

.timeline-content:hover {
  transform: translateY(-1px);
  box-shadow: var(--box-shadow-light);
}

.timeline-title {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color-primary);
}

.timeline-subtitle {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: var(--text-color-secondary);
}

.timeline-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.deadline-label {
  padding: 2px 8px;
  border-radius: var(--border-radius-base);
  font-size: 12px;
  font-weight: 500;
}

.deadline-label.danger {
  background-color: rgba(245, 108, 108, 0.1);
  color: var(--danger-color);
}

.deadline-label.warning {
  background-color: rgba(230, 162, 60, 0.1);
  color: var(--warning-color);
}

.deadline-label.success {
  background-color: rgba(103, 194, 58, 0.1);
  color: var(--success-color);
}

.resource-type,
.resource-size {
  color: var(--text-color-secondary);
  padding: 2px 8px;
  border-radius: var(--border-radius-base);
  background-color: var(--background-color-secondary);
}

.more-link {
  font-size: 14px;
  color: var(--primary-color);
  text-decoration: none;
  display: flex;
  align-items: center;
  transition: all var(--transition-base);
}

.more-link:hover {
  opacity: 0.8;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  .dashboard-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
    color: #409eff;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-container {
    height: 300px;
  }
  
  .number {
    font-size: 36px;
  }
  
  .card-content {
    padding: 20px 0;
  }
  
  .timeline-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    padding: 12px;
  }
  
  .number {
    font-size: 28px;
  }
  
  .label {
    font-size: 14px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>
