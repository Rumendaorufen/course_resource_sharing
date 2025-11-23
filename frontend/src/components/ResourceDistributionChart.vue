<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>资源分布统计</span>
      </div>
    </template>
    <div ref="chart" style="width: 100%; height: 300px;"></div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import * as echarts from 'echarts';
import analyticsApi from '@/api/analytics';

const chart = ref(null);
let myChart = null;
let resizeHandler = null;

const props = defineProps({
  refreshTrigger: {
    type: Number,
    default: 0
  }
});

const fetchData = async () => {
  try {
    // 尝试不同的API获取数据
    let res;
    try {
      res = await analyticsApi.getCourseDistribution();
    } catch (apiError) {
      console.warn('getCourseDistribution API失败:', apiError);
      throw apiError; // 直接抛出错误，让外层catch处理
    }
    
    // 处理不同格式的API返回
    let chartData = [];
    
    // 更健壮的数据格式处理
    if (res) {
      // 检查是否为成功响应
      if (res.code === 200 || res.success) {
        // 提取数据部分
        const dataPart = res.data || res.result || {};
        
        // 特殊情况：如果数据是纯数字
        if (typeof dataPart === 'number') {
          chartData = [{ name: '总资源数', value: dataPart }];
        }
        // 情况1: 数据直接是数组
        else if (Array.isArray(dataPart)) {
          chartData = dataPart.map(item => ({
            name: item.courseName || item.name || `课程${item.course_id || item.id}` || '未知课程',
            value: Number(item.resource_count ?? item.resourceCount ?? item.count ?? item.value ?? 0)
          }));
        }
        // 情况2: 数据对象中包含data数组
        else if (dataPart.data && Array.isArray(dataPart.data)) {
          chartData = dataPart.data.map(item => ({
            name: item.courseName || item.name || `课程${item.course_id || item.id}` || '未知课程',
            value: Number(item.resource_count ?? item.resourceCount ?? item.count ?? item.value ?? 0)
          }));
        }
        // 情况3: 数据对象中包含list数组
        else if (dataPart.list && Array.isArray(dataPart.list)) {
          chartData = dataPart.list.map(item => ({
            name: item.courseName || item.name || `课程${item.course_id || item.id}` || '未知课程',
            value: Number(item.resource_count ?? item.resourceCount ?? item.count ?? item.value ?? 0)
          }));
        }
        // 情况4: 数据对象本身是键值对格式
        else if (typeof dataPart === 'object' && dataPart !== null) {
          chartData = Object.entries(dataPart)
            .filter(([key, value]) => value !== null && value !== undefined)
            .map(([name, value]) => ({
              name: name,
              value: Number(value) || 0
            }));
        }
      }
    }
    
    console.log('Generated chartData:', chartData); // 跟踪 chartData 的生成过程
    try {
      updateChart(chartData);
    } catch (e) {
      console.error('ResourceDistributionChart: Error updating chart after data fetch:', e);
    }
  } catch (error) {
    console.error('获取资源分布数据异常:', error);
    // 记录错误但仍然调用updateChart以显示无数据状态
    try {
      updateChart([]);
    } catch (e) {
      console.error('ResourceDistributionChart: Error updating chart with empty data after fetch error:', e);
    }
  }
};

const updateChart = (data) => {
  // 确保data是数组
  const chartData = Array.isArray(data) ? data : [];
  
  // 在设置图表选项前先确保图表实例存在
  if (!myChart && chart.value) {
    // 销毁已有实例（如果存在）
    if (myChart) {
      myChart.dispose();
    }
    // 创建新实例
    myChart = echarts.init(chart.value);
  }
  
  if (!myChart) return;
  
  // 计算总数用于百分比显示
  const total = chartData.reduce((sum, item) => sum + (Number(item.value) || 0), 0);
  const hasData = chartData.length > 0 && total > 0;
  
  // 设置图表配置
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        if (!params || !params.name || params.value === undefined) {
          return '数据无效';
        }
        const percentage = total > 0 ? ((Number(params.value) / total) * 100).toFixed(2) : '0.00';
        return `${params.name}: ${params.value} 个 (${percentage}%)`;
      },
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      textStyle: {
        color: '#fff'
      },
      borderColor: '#ddd',
      borderWidth: 1
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        fontSize: 12
      }
    },
    series: hasData ? [
      {
        name: '资源数量',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['60%', '50%'],
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 15,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.6)'
          },
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: function(idx) {
          return idx * 50;
        }
      }
    ] : [],
    // 无数据时显示提示
    graphic: hasData ? [] : [
      {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '暂无数据',
          fill: '#999',
          fontSize: 14
        }
      }
    ],
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#C0C4CC', '#909399', '#67C23A', '#409EFF']
  };
  
  // 直接设置图表选项，不需要clear
  try {
    myChart.setOption(option, true);
    console.log('ResourceDistributionChart: ECharts option set successfully.');
  } catch (e) {
    console.error('ResourceDistributionChart: Error setting ECharts option:', e);
  }
};

onMounted(() => {
  console.log('ResourceDistributionChart: Component mounted.');
  nextTick(() => {
    console.log('ResourceDistributionChart: nextTick callback executed.');
    if (chart.value) {
      console.log('ResourceDistributionChart: chart.value is available.', chart.value);
      // 初始化图表实例
      try {
        myChart = echarts.init(chart.value);
        console.log('ResourceDistributionChart: ECharts instance initialized.');
      } catch (e) {
        console.error('ResourceDistributionChart: Error initializing ECharts:', e);
      }

      // 设置响应式处理
      resizeHandler = () => {
        if (myChart) {
          myChart.resize();
        }
      };
      window.addEventListener('resize', resizeHandler);

      // 初始化数据
      fetchData();
    } else {
      console.warn('ResourceDistributionChart: chart.value is null or undefined.');
    }
  });
});

onBeforeUnmount(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler);
    resizeHandler = null;
  }
});

watch(() => props.refreshTrigger, () => {
  fetchData();
});
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>