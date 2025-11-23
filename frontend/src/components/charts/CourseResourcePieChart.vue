<template>
  <div class="course-resource-pie-chart">
    <div class="chart-header">
      <h3>课程资源分布统计</h3>
      <el-button size="small" type="primary" @click="refreshData">刷新数据</el-button>
    </div>
    <div ref="chartRef" class="chart-container" :style="{ width: width, height: height }"></div>
    <div v-if="loading" class="loading-overlay">
      <el-loading-spinner></el-loading-spinner>
    </div>
    <div v-if="error" class="error-message">
      <el-alert
        title="数据加载失败"
        description="请稍后重试"
        type="error"
        show-icon
        :closable="false"
      ></el-alert>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import request from '@/utils/request'

export default {
  name: 'CourseResourcePieChart',
  props: {
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '400px'
    }
  },
  data() {
    return {
      chartInstance: null,
      loading: false,
      error: false,
      chartData: []
      // 移除不必要的courseMap映射表
    }
  },
  async mounted() {
    this.initChart()
    this.fetchData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    // 移除loadCourseMap方法，因为不再需要课程ID映射
    
    initChart() {
      this.chartInstance = echarts.init(this.$refs.chartRef)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#ddd',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          }
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          textStyle: {
            fontSize: 12
          },
          formatter: function(name) {
            const data = this.chartData.find(item => item.name === name)
            return `${name}: ${data ? data.value : 0}`
          }.bind(this)
        },
        series: [
          {
            name: '课程资源分布',
            type: 'pie',
            radius: ['30%', '70%'],
            avoidLabelOverlap: false,
            center: ['40%', '50%'],
            itemStyle: {
              borderRadius: 8,
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
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: []
          }
        ],
        color: [
          '#5470c6', '#91cc75', '#fac858', '#ee6666', 
          '#73c0de', '#3ba272', '#fc8452', '#9a60b4', 
          '#ea7ccc', '#67C23A', '#E6A23C', '#F56C6C'
        ]
      }
      
      this.chartInstance.setOption(option)
    },
    async fetchData() {
      this.loading = true
      this.error = false
      try {
        // 发送API请求获取课程资源分布数据
        const response = await request.get('/resource/stats/countByCourse')
        console.log('完整API响应:', JSON.stringify(response))
        
        let courseData = []
        
        // 检查响应的结构，适配后端返回的course_name格式
        if (response) {
          // 如果响应是数组且包含course_name字段，直接使用
          if (Array.isArray(response) && response.length > 0 && response[0].course_name !== undefined) {
            courseData = response
          }
          // 如果response.data是数组且包含course_name字段
          else if (response.data && Array.isArray(response.data) && response.data.length > 0 && response.data[0].course_name !== undefined) {
            courseData = response.data
          }
          // 如果是标准响应格式，尝试从更深层次提取数据
          else if (response.data && response.data.code === 200 && response.data.data) {
            if (Array.isArray(response.data.data)) {
              courseData = response.data.data
            }
          }
        }
        
        console.log('找到的课程数据:', courseData)
        
        // 如果没有找到有效的课程数据，使用模拟数据进行测试
        if (courseData.length === 0) {
          console.log('使用模拟数据进行测试')
          courseData = [
            { course_name: 'c++', resource_count: 1 },
            { course_name: 'java', resource_count: 1 },
            { course_name: '数据结构', resource_count: 1 }
          ]
        }
        
        // 合并相同课程名称的数据
        const mergedData = this.mergeCourseData(courseData)
        
        // 转换数据为图表需要的格式
        this.chartData = mergedData.map(item => ({
          name: item.course_name,
          value: Number(item.resource_count) || 0
        })).filter(item => item.value > 0)
        
        console.log('图表数据:', this.chartData)
        
        // 更新图表
        this.updateChart()
        
      } catch (error) {
        console.error('获取课程资源分布数据失败:', error)
        
        // 设置错误状态
        this.error = true
        
        // 使用模拟数据显示图表
        this.chartData = [
          { name: 'c++', value: 1 },
          { name: 'java', value: 1 },
          { name: '数据结构', value: 1 }
        ]
        
        this.updateChart()
      } finally {
        this.loading = false
      }
    },
    // 新增合并相同课程名称数据的方法
    mergeCourseData(data) {
      const merged = {}
      
      data.forEach(item => {
        const courseName = item.course_name
        const resourceCount = Number(item.resource_count) || 0
        
        if (merged[courseName]) {
          // 如果课程已存在，累加资源数量
          merged[courseName] += resourceCount
        } else {
          // 否则，创建新的课程记录
          merged[courseName] = resourceCount
        }
      })
      
      // 转换回数组格式
      return Object.keys(merged).map(courseName => ({
        course_name: courseName,
        resource_count: merged[courseName]
      }))
    },
    updateChart() {
      if (this.chartInstance) {
        this.chartInstance.setOption({
          series: [{
            data: this.chartData
          }],
          legend: {
            formatter: function(name) {
              const data = this.chartData.find(item => item.name === name)
              return `${name}: ${data ? data.value : 0}`
            }.bind(this)
          }
        })
      }
    },
    refreshData() {
      this.fetchData()
    },
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    }
  }
}
</script>

<style scoped>
.course-resource-pie-chart {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.chart-container {
  margin: 0 auto;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  z-index: 10;
}

.error-message {
  margin-top: 20px;
}
</style>