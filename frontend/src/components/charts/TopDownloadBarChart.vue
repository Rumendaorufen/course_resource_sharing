<template>
  <div class="top-download-bar-chart">
    <div class="chart-header">
      <h3>资源下载比例统计</h3>
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
  name: 'TopDownloadBarChart',
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
      chartData: [], // 直接使用后端返回的数据格式
      rawData: [], // 存储原始数据用于调试
      totalDownloads: 0
    }
  },
  mounted() {
    // 延迟初始化，确保DOM已完全渲染
    this.$nextTick(() => {
      this.initChart()
      this.fetchData()
      window.addEventListener('resize', this.handleResize)
    })
  },
  beforeUnmount() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    // 初始化图表
    initChart() {
      console.log('初始化图表...')
      try {
        // 确保DOM元素存在且有效
        if (!this.$refs.chartRef || this.$refs.chartRef.clientWidth === 0 || this.$refs.chartRef.clientHeight === 0) {
          console.error('图表容器未找到或尺寸为0')
          // 尝试在延迟后重新初始化
          setTimeout(() => this.initChart(), 300)
          return
        }
        
        // 如果已有实例，先销毁
        if (this.chartInstance) {
          try {
            this.chartInstance.dispose()
          } catch (e) {
            console.error('销毁旧图表实例失败:', e)
          }
          this.chartInstance = null
        }
        
        // 创建新实例
        this.chartInstance = echarts.init(this.$refs.chartRef)
        console.log('图表实例创建成功')
        
        // 基本的空数据配置
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: (params) => {
              if (!Array.isArray(params) || params.length === 0 || !params[0]) return '无数据'
              const data = params[0]
              const percentage = this.calculatePercentage(data.value || 0)
              return `${data.name || '未知'}<br/>下载量: ${data.value || 0}<br/>占比: ${percentage}`
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: ['暂无数据'],
            axisLabel: {
              interval: 0,
              rotate: 45,
              fontSize: 12
            }
          },
          yAxis: {
            type: 'value',
            name: '下载次数',
            min: 0
          },
          series: [{
            name: '下载量',
            type: 'bar',
            data: [0],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#409EFF' },
                { offset: 1, color: '#67C23A' }
              ]),
              borderRadius: [4, 4, 0, 0]
            },
            label: {
              show: false,
              fontSize: 10
            },
            barWidth: '60%'
          }]
        }
        
        // 设置初始选项
        this.chartInstance.setOption(option, true)
        console.log('图表初始化完成')
        
        // 如果已有数据，立即更新图表
        if (this.chartData && this.chartData.length > 0) {
          console.log('初始化后立即更新图表数据')
          this.updateChart()
        }
      } catch (error) {
        console.error('图表初始化失败:', error)
        // 重置图表实例
        this.chartInstance = null
        // 增加重试机制
        setTimeout(() => this.initChart(), 500)
      }
    },
    
    // 计算百分比
    calculatePercentage(value) {
      // 更严格的数值验证
      if (typeof this.totalDownloads !== 'number' || this.totalDownloads === 0 || 
          typeof value !== 'number' || isNaN(value) || value < 0) return '0%'
      return `${((value / this.totalDownloads) * 100).toFixed(1)}%`
    },
    
    // 获取数据
    async fetchData() {
      console.log('开始获取数据...')
      this.loading = true
      this.error = false
      
      try {
        // 使用原有的request工具调用API，保持原有的调用路径
        const response = await request.get('/resource/by-course')
        console.log('API响应结果:', JSON.stringify(response))
        
        // 增强的响应格式验证
        if (!response) {
          console.error('响应为空')
          throw new Error('响应为空')
        }
        
        // 处理可能的数据嵌套格式
        let result = response
        if (response.data && response.data.code !== undefined) {
          result = response.data
        }
        
        console.log('标准化后的响应格式:', JSON.stringify(result))
        
        // 检查响应格式
        if (result.code === 200 && Array.isArray(result.data)) {
          console.log('响应格式正确，获取到数据项数:', result.data.length)
          
          // 存储原始数据
          this.rawData = result.data
          
          // 更严格的数据格式验证
          const validData = result.data.filter(item => {
            if (!item || typeof item !== 'object') return false
            return item.name && typeof item.name === 'string' && 
                   item.value !== undefined && typeof item.value === 'number'
          })
          
          console.log('有效数据项数:', validData.length)
          
          // 计算总下载量
          this.totalDownloads = validData.reduce((sum, item) => sum + item.value, 0)
          console.log('总下载量:', this.totalDownloads)
          
          // 直接使用格式化后的数据
          this.chartData = validData
          
          // 更新图表
          this.updateChart()
        } else {
          console.error('响应格式不正确:', result)
          this.chartData = []
          this.rawData = []
          this.totalDownloads = 0
          this.updateChart()
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.error = true
        this.chartData = []
        this.rawData = []
        this.totalDownloads = 0
        this.updateChart()
      } finally {
        this.loading = false
      }
    },
    
    // 更新图表
    updateChart() {
      console.log('更新图表数据:', this.chartData)
      
      try {
        // 确保图表实例存在且为有效的ECharts实例
        if (!this.chartInstance || typeof this.chartInstance.setOption !== 'function') {
          console.error('图表实例不存在或无效，重新初始化')
          // 确保先销毁可能存在的无效实例
          if (this.chartInstance) {
            try {
              this.chartInstance.dispose()
            } catch (e) {
              console.error('销毁无效图表实例失败:', e)
            }
            this.chartInstance = null
          }
          // 延迟初始化，确保DOM稳定
          setTimeout(() => {
            this.initChart()
          }, 100)
          return
        }
        
        // 安全地提取数据
        const courseNames = Array.isArray(this.chartData) ? 
          this.chartData.map(item => item && item.name ? String(item.name) : '未知').filter(Boolean) : []
        const downloadCounts = Array.isArray(this.chartData) ? 
          this.chartData.map(item => item && typeof item.value === 'number' ? item.value : 0) : []
        
        console.log('处理后的图表数据 - 课程名称:', courseNames)
        console.log('处理后的图表数据 - 下载量:', downloadCounts)
        
        // 完整的图表选项，包含所有必需配置
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: (params) => {
              if (!Array.isArray(params) || params.length === 0 || !params[0]) return '无数据'
              const data = params[0]
              const percentage = this.calculatePercentage(data.value || 0)
              return `${data.name || '未知'}<br/>下载量: ${data.value || 0}<br/>占比: ${percentage}`
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: courseNames.length > 0 ? courseNames : ['暂无数据'],
            axisLabel: {
              interval: 0,
              rotate: 45,
              fontSize: 12
            }
          },
          yAxis: {
            type: 'value',
            name: '下载次数',
            min: 0
          },
          series: [{
            name: '下载量',
            type: 'bar',
            data: downloadCounts.length > 0 ? downloadCounts : [0],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#409EFF' },
                { offset: 1, color: '#67C23A' }
              ]),
              borderRadius: [4, 4, 0, 0]
            },
            label: {
              show: courseNames.length > 0,
              position: 'top',
              formatter: (params) => {
                if (!params || typeof params.value !== 'number') return ''
                const percentage = this.calculatePercentage(params.value)
                return `${params.value}\n(${percentage})`
              },
              fontSize: 10
            },
            barWidth: '60%'
          }]
        }
        
        // 尝试在更新前检查图表容器是否仍然存在
        if (this.$refs.chartRef && this.chartInstance.getDom() && this.$refs.chartRef.contains(this.chartInstance.getDom())) {
          // 先清空画布，避免数据残留
          this.chartInstance.clear()
          // 然后设置新选项
          this.chartInstance.setOption(option, true) // 第二个参数true表示不合并配置
          console.log('图表更新完成')
        } else {
          console.error('图表容器已不存在或已移除')
          throw new Error('图表容器不可用')
        }
      } catch (error) {
        console.error('更新图表失败:', error)
        // 尝试重新初始化，使用更长的延迟确保DOM已稳定
        if (this.chartInstance) {
          try {
            this.chartInstance.dispose()
          } catch (e) {
            console.error('销毁图表实例失败:', e)
          }
          this.chartInstance = null
        }
        // 使用更长的延迟重新初始化
        setTimeout(() => this.initChart(), 300)
      }
    },
    
    // 刷新数据
    refreshData() {
      console.log('手动刷新数据...')
      this.fetchData()
    },
    
    // 处理窗口大小变化
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    }
  }
}
</script>

<style scoped>
.top-download-bar-chart {
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

/* 调试信息样式 */
.debug-info {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
}

.debug-info h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.debug-info pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: #303133;
  font-family: monospace;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .chart-header h3 {
    font-size: 16px;
  }
  
  .debug-info {
    display: none; /* 在移动端隐藏调试信息 */
  }
}
</style>