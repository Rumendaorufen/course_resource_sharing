<template>
  <div class="chart-container-wrapper">
    <div v-if="loading" class="chart-loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div v-else-if="error" class="chart-error">
      <el-icon class="error-icon"><WarningFilled /></el-icon>
      <span>{{ error }}</span>
    </div>
    <div v-else ref="chartRef" class="chart-wrapper" :style="{ height: chartHeight || '400px' }"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { Loading, WarningFilled } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  chartType: {
    type: String,
    required: true,
    validator: (value) => ['pie', 'bar', 'line', 'lineBar', 'scatter'].includes(value)
  },
  chartData: {
    type: Object,
    default: () => ({})
  },
  chartHeight: {
    type: String,
    default: '400px'
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  },
  showLegend: {
    type: Boolean,
    default: true
  },
  legendPosition: {
    type: String,
    default: 'top'
  },
  animation: {
    type: Boolean,
    default: true
  },
  themeAware: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['chartInitialized', 'chartClick', 'chartHover'])

// Chart reference
const chartRef = ref(null)
let chartInstance = null

// 计算当前主题
const isDarkTheme = computed(() => {
  return document.documentElement.classList.contains('dark')
})

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
  const tooltipBackground = getComputedStyle(document.documentElement).getPropertyValue('--tooltip-background').trim()
  const tooltipTextColor = getComputedStyle(document.documentElement).getPropertyValue('--tooltip-text-color').trim()
  
  return {
    baseColors,
    colorPalette,
    backgroundColor,
    textColor,
    axisColor,
    tooltipBackground,
    tooltipTextColor
  }
}

// 创建饼图配置
const createPieChartOption = (data, themeConfig) => {
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: props.chartData.tooltipFormatter || '{a} <br/>{b}: {c} ({d}%)',
      backgroundColor: themeConfig.tooltipBackground,
      textStyle: {
        color: themeConfig.tooltipTextColor
      },
      borderColor: themeConfig.axisColor
    },
    legend: props.showLegend ? {
      orient: props.legendPosition.includes('left') || props.legendPosition.includes('right') ? 'vertical' : 'horizontal',
      left: props.legendPosition.includes('left') ? 'left' : 'center',
      right: props.legendPosition.includes('right') ? 'right' : 'auto',
      top: props.legendPosition.includes('top') ? 'top' : 'auto',
      bottom: props.legendPosition.includes('bottom') ? 'bottom' : 'auto',
      textStyle: {
        fontSize: 12,
        color: themeConfig.textColor
      }
    } : false,
    series: [
      {
        name: props.chartData.title || '数据分布',
        type: 'pie',
        radius: props.chartData.radius || ['40%', '70%'],
        center: props.chartData.center || ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: themeConfig.backgroundColor,
          borderWidth: 2,
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.1)'
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold',
            color: themeConfig.textColor
          },
          itemStyle: {
            shadowBlur: 15,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        labelLine: {
          show: false
        },
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: function (idx) {
          return Math.random() * 200;
        },
        data: data || []
      }
    ],
    color: themeConfig.colorPalette
  }
}

// 创建柱状图配置
const createBarChartOption = (data, themeConfig) => {
  const categories = props.chartData.categories || []
  const seriesData = props.chartData.series || []
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(0, 0, 0, 0.1)'
        }
      },
      backgroundColor: themeConfig.tooltipBackground,
      textStyle: {
        color: themeConfig.tooltipTextColor
      },
      borderColor: themeConfig.axisColor
    },
    legend: props.showLegend && seriesData.length > 1 ? {
      data: seriesData.map(s => s.name),
      textStyle: {
        color: themeConfig.textColor
      },
      top: props.legendPosition.includes('top') ? 0 : 'auto',
      bottom: props.legendPosition.includes('bottom') ? 0 : 'auto'
    } : false,
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: props.showLegend && seriesData.length > 1 ? '15%' : '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: {
        lineStyle: {
          color: themeConfig.axisColor
        }
      },
      axisLabel: {
        color: themeConfig.textColor,
        interval: 0,
        rotate: props.chartData.rotateLabels || 0
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
    series: seriesData.map((series, index) => ({
      name: series.name,
      type: 'bar',
      data: series.data,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: themeConfig.colorPalette[index] },
          { offset: 1, color: themeConfig.colorPalette[index] + '80' }
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
        return idx * 100 + (index * 50);
      }
    })),
    animationEasing: 'elasticOut',
    animationDelayUpdate: function (idx) {
      return idx * 5;
    }
  }
}

// 创建折线图配置
const createLineChartOption = (data, themeConfig) => {
  const categories = props.chartData.categories || []
  const seriesData = props.chartData.series || []
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: themeConfig.axisColor
        }
      },
      backgroundColor: themeConfig.tooltipBackground,
      textStyle: {
        color: themeConfig.tooltipTextColor
      },
      borderColor: themeConfig.axisColor
    },
    legend: props.showLegend && seriesData.length > 1 ? {
      data: seriesData.map(s => s.name),
      textStyle: {
        color: themeConfig.textColor
      },
      top: props.legendPosition.includes('top') ? 0 : 'auto',
      bottom: props.legendPosition.includes('bottom') ? 0 : 'auto'
    } : false,
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: props.showLegend && seriesData.length > 1 ? '15%' : '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: {
        lineStyle: {
          color: themeConfig.axisColor
        }
      },
      axisLabel: {
        color: themeConfig.textColor,
        interval: 0,
        rotate: props.chartData.rotateLabels || 0
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
    series: seriesData.map((series, index) => ({
      name: series.name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: themeConfig.colorPalette[index]
      },
      itemStyle: {
        color: themeConfig.colorPalette[index],
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: themeConfig.colorPalette[index] + '40' },
          { offset: 1, color: themeConfig.colorPalette[index] + '10' }
        ])
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      animationDelay: function (idx) {
        return idx * 100 + (index * 50);
      },
      data: series.data
    })),
    animationEasing: 'elasticOut',
    animationDelayUpdate: function (idx) {
      return idx * 5;
    }
  }
}

// 创建折线柱状图混合配置
const createLineBarChartOption = (data, themeConfig) => {
  const categories = props.chartData.categories || []
  const barSeries = (props.chartData.barSeries || []).map((series, index) => ({
    ...series,
    type: 'bar',
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: themeConfig.colorPalette[index] },
        { offset: 1, color: themeConfig.colorPalette[index] + '80' }
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
  }))
  
  const lineSeries = (props.chartData.lineSeries || []).map((series, index) => ({
    ...series,
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 8,
    lineStyle: {
      width: 3,
      color: themeConfig.colorPalette[barSeries.length + index]
    },
    itemStyle: {
      color: themeConfig.colorPalette[barSeries.length + index],
      borderWidth: 2,
      borderColor: '#fff'
    },
    emphasis: {
      focus: 'series',
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.3)'
      }
    },
    animationDelay: function (idx) {
      return idx * 100 + 50;
    }
  }))
  
  const allSeries = [...barSeries, ...lineSeries]
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: themeConfig.axisColor
        }
      },
      backgroundColor: themeConfig.tooltipBackground,
      textStyle: {
        color: themeConfig.tooltipTextColor
      },
      borderColor: themeConfig.axisColor
    },
    legend: props.showLegend ? {
      data: allSeries.map(s => s.name),
      textStyle: {
        color: themeConfig.textColor
      },
      top: props.legendPosition.includes('top') ? 0 : 'auto',
      bottom: props.legendPosition.includes('bottom') ? 0 : 'auto'
    } : false,
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: props.showLegend ? '15%' : '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: {
        lineStyle: {
          color: themeConfig.axisColor
        }
      },
      axisLabel: {
        color: themeConfig.textColor,
        interval: 0,
        rotate: props.chartData.rotateLabels || 0
      }
    },
    yAxis: props.chartData.useMultipleYAxis ? [
      {
        type: 'value',
        name: props.chartData.yAxisLeftName || '',
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
      {
        type: 'value',
        name: props.chartData.yAxisRightName || '',
        position: 'right',
        axisLine: {
          lineStyle: {
            color: themeConfig.axisColor
          }
        },
        axisLabel: {
          color: themeConfig.textColor
        },
        splitLine: {
          show: false
        }
      }
    ] : {
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
    series: allSeries,
    animationEasing: 'elasticOut',
    animationDelayUpdate: function (idx) {
      return idx * 5;
    }
  }
}

// 创建散点图配置
const createScatterChartOption = (data, themeConfig) => {
  const seriesData = props.chartData.series || []
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: themeConfig.tooltipBackground,
      textStyle: {
        color: themeConfig.tooltipTextColor
      },
      borderColor: themeConfig.axisColor,
      formatter: props.chartData.tooltipFormatter || '{b}: ({c0}, {c1})'
    },
    legend: props.showLegend && seriesData.length > 1 ? {
      data: seriesData.map(s => s.name),
      textStyle: {
        color: themeConfig.textColor
      },
      top: props.legendPosition.includes('top') ? 0 : 'auto',
      bottom: props.legendPosition.includes('bottom') ? 0 : 'auto'
    } : false,
    grid: {
      left: '3%',
      right: '7%',
      bottom: '3%',
      top: props.showLegend && seriesData.length > 1 ? '15%' : '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: props.chartData.xAxisName || '',
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
    yAxis: {
      type: 'value',
      name: props.chartData.yAxisName || '',
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
    series: seriesData.map((series, index) => ({
      name: series.name,
      type: 'scatter',
      symbolSize: series.symbolSize || 20,
      itemStyle: {
        color: themeConfig.colorPalette[index],
        opacity: 0.8,
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.3)'
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 15,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: series.data,
      animationDelay: function (idx) {
        return Math.random() * 200 + (index * 100);
      }
    })),
    animationEasing: 'elasticOut',
    animationDelayUpdate: function (idx) {
      return idx * 5;
    }
  }
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  // 销毁已有实例
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  // 创建新实例
  chartInstance = echarts.init(chartRef.value)
  
  // 获取主题配置
  const themeConfig = getChartThemeConfig()
  
  // 根据图表类型创建配置
  let option = {}
  const chartData = props.chartData.data || []
  
  switch (props.chartType) {
    case 'pie':
      option = createPieChartOption(chartData, themeConfig)
      break
    case 'bar':
      option = createBarChartOption(chartData, themeConfig)
      break
    case 'line':
      option = createLineChartOption(chartData, themeConfig)
      break
    case 'lineBar':
      option = createLineBarChartOption(chartData, themeConfig)
      break
    case 'scatter':
      option = createScatterChartOption(chartData, themeConfig)
      break
    default:
      console.error('不支持的图表类型:', props.chartType)
  }
  
  // 应用配置
  chartInstance.setOption(option)
  
  // 绑定事件
  chartInstance.on('click', (params) => {
    emit('chartClick', params)
  })
  
  chartInstance.on('mouseover', (params) => {
    emit('chartHover', params)
  })
  
  // 通知父组件图表已初始化
  emit('chartInitialized', chartInstance)
}

// 窗口大小变化处理
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 主题变化处理
const handleThemeChange = () => {
  if (props.themeAware) {
    initChart()
  }
}

// 监听数据变化
watch(() => props.chartData, () => {
  nextTick(() => {
    initChart()
  })
}, { deep: true })

// 监听主题变化
watch(isDarkTheme, () => {
  handleThemeChange()
})

// 组件挂载
onMounted(() => {
  nextTick(() => {
    initChart()
  })
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
  
  // 添加主题变化监听
  window.addEventListener('themeChanged', handleThemeChange)
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('themeChanged', handleThemeChange)
  
  // 销毁图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 暴露方法给父组件
defineExpose({
  refreshChart: initChart,
  getChartInstance: () => chartInstance
})
</script>

<style scoped>
.chart-container-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.chart-wrapper {
  width: 100%;
  transition: all var(--transition-base);
}

.chart-loading,
.chart-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  font-size: 16px;
  color: var(--text-color-secondary);
  gap: 8px;
  flex-direction: column;
}

.loading-icon {
  font-size: 32px;
  color: var(--primary-color);
  animation: rotate 2s linear infinite;
}

.error-icon {
  font-size: 32px;
  color: var(--danger-color);
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-container-wrapper {
    min-height: 300px;
  }
  
  .chart-loading,
  .chart-error {
    height: 300px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .chart-container-wrapper {
    min-height: 250px;
  }
  
  .chart-loading,
  .chart-error {
    height: 250px;
    font-size: 12px;
  }
  
  .loading-icon,
  .error-icon {
    font-size: 24px;
  }
}
</style>