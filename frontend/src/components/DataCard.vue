<template>
  <el-card 
    class="data-card"
    :shadow="shadow"
    :class="{ 'data-card-hover': hoverEffect }"
    @click="handleCardClick"
  >
    <div class="data-card-content">
      <div class="data-card-header">
        <div class="data-card-title">{{ title }}</div>
        <div v-if="subtitle" class="data-card-subtitle">{{ subtitle }}</div>
      </div>
      
      <div class="data-card-body">
        <div class="data-card-value-wrapper">
          <div class="data-card-value" :class="{ 'has-trend': showTrend }">
            <span v-if="prefix" class="data-card-prefix">{{ prefix }}</span>
            {{ displayValue }}
            <span v-if="suffix" class="data-card-suffix">{{ suffix }}</span>
          </div>
          <div v-if="showTrend && trendData" class="data-card-trend">
            <el-icon :class="trendIconClass">
              <component :is="trendIcon" />
            </el-icon>
            <span :class="trendTextClass">{{ trendData.value }}{{ trendData.unit || '%' }}</span>
          </div>
        </div>
        
        <div v-if="showIcon" class="data-card-icon">
          <el-icon :size="iconSize"><component :is="icon" /></el-icon>
        </div>
      </div>
      
      <div v-if="showProgress && progressData" class="data-card-progress">
        <el-progress 
          :percentage="progressData.percentage" 
          :color="progressData.color"
          :stroke-width="progressData.strokeWidth || 4"
          :show-text="progressData.showText !== false"
          :text-inside="progressData.textInside"
        />
        <div v-if="progressData.label" class="progress-label">{{ progressData.label }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowUpBold, ArrowDownBold, ArrowRightBold } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 卡片标题
  title: {
    type: String,
    required: true
  },
  
  // 卡片副标题
  subtitle: {
    type: String,
    default: ''
  },
  
  // 显示的值
  value: {
    type: [Number, String],
    required: true
  },
  
  // 数值前缀
  prefix: {
    type: String,
    default: ''
  },
  
  // 数值后缀
  suffix: {
    type: String,
    default: ''
  },
  
  // 是否显示图标
  showIcon: {
    type: Boolean,
    default: true
  },
  
  // 图标组件
  icon: {
    type: Object,
    required: true
  },
  
  // 图标大小
  iconSize: {
    type: Number,
    default: 36
  },
  
  // 阴影样式
  shadow: {
    type: String,
    default: 'hover',
    validator: (value) => ['always', 'hover', 'never'].includes(value)
  },
  
  // 是否启用悬停效果
  hoverEffect: {
    type: Boolean,
    default: true
  },
  
  // 是否可点击
  clickable: {
    type: Boolean,
    default: false
  },
  
  // 是否显示趋势
  showTrend: {
    type: Boolean,
    default: false
  },
  
  // 趋势数据 { value: number, unit: string, type: 'increase' | 'decrease' | 'equal' }
  trendData: {
    type: Object,
    default: () => ({})
  },
  
  // 是否显示进度条
  showProgress: {
    type: Boolean,
    default: false
  },
  
  // 进度数据 { percentage: number, color: string, strokeWidth: number, showText: boolean, textInside: boolean, label: string }
  progressData: {
    type: Object,
    default: () => ({})
  },
  
  // 数值格式化类型
  formatType: {
    type: String,
    default: 'normal',
    validator: (value) => ['normal', 'number', 'currency', 'percentage', 'time'].includes(value)
  },
  
  // 数值精度（小数位数）
  precision: {
    type: Number,
    default: 0
  },
  
  // 卡片类型
  type: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'success', 'warning', 'danger', 'info'].includes(value)
  },
  
  // 动画效果
  animation: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['click'])

// 格式化显示值
const displayValue = computed(() => {
  let formattedValue = props.value
  
  if (props.formatType === 'number' && typeof props.value === 'number') {
    formattedValue = props.value.toFixed(props.precision)
  } else if (props.formatType === 'currency') {
    if (typeof props.value === 'number') {
      formattedValue = `¥${props.value.toFixed(props.precision)}`
    }
  } else if (props.formatType === 'percentage') {
    if (typeof props.value === 'number') {
      formattedValue = `${(props.value * 100).toFixed(props.precision)}%`
    }
  } else if (props.formatType === 'time') {
    // 可以根据需要添加时间格式化逻辑
  }
  
  return formattedValue
})

// 趋势图标
const trendIcon = computed(() => {
  if (!props.trendData || !props.trendData.type) return ArrowRightBold
  
  switch (props.trendData.type) {
    case 'increase':
      return ArrowUpBold
    case 'decrease':
      return ArrowDownBold
    default:
      return ArrowRightBold
  }
})

// 趋势图标样式类
const trendIconClass = computed(() => {
  if (!props.trendData || !props.trendData.type) return 'trend-equal'
  
  return `trend-${props.trendData.type}`
})

// 趋势文本样式类
const trendTextClass = computed(() => {
  if (!props.trendData || !props.trendData.type) return 'trend-equal-text'
  
  return `trend-${props.trendData.type}-text`
})

// 处理卡片点击
const handleCardClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style scoped>
.data-card {  
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  border-radius: 12px;
  overflow: hidden;
  cursor: default;
  position: relative;
  transform-origin: center;
}

.data-card.clickable {
  cursor: pointer;
}

.data-card-hover:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15) !important;
  transition: all 0.3s ease;
}

.data-card:active {
  transform: translateY(-2px) scale(0.99);
  transition: all 0.1s ease;
}

/* 数值变化动画 */
.data-card-value {
  transition: all 0.5s ease;
}

/* 图标微交互 */
.data-card-icon {
  transition: all 0.3s ease;
}

.data-card:hover .data-card-icon {
  transform: scale(1.1) rotate(5deg);
}

.data-card-content {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.data-card-header {
  margin-bottom: 16px;
}

.data-card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color-secondary);
  margin-bottom: 4px;
}

.data-card-subtitle {
  font-size: 12px;
  color: var(--text-color-placeholder);
}

.data-card-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
}

.data-card-value-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.data-card-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-color-primary);
  transition: all var(--transition-base);
  line-height: 1.2;
}

.data-card-value.has-trend {
  font-size: 24px;
}

.data-card-prefix,
.data-card-suffix {
  font-size: 16px;
  font-weight: 400;
  color: var(--text-color-secondary);
}

.data-card-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.trend-increase,
.trend-increase-text {
  color: var(--success-color);
}

.trend-decrease,
.trend-decrease-text {
  color: var(--danger-color);
}

.trend-equal,
.trend-equal-text {
  color: var(--text-color-secondary);
}

.data-card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary-color-light) 0%, var(--primary-color-light-90) 100%);
  color: var(--primary-color);
  transition: all var(--transition-base);
}

.data-card[data-type="success"] .data-card-icon {
  background: linear-gradient(135deg, var(--success-color-light) 0%, var(--success-color-light-90) 100%);
  color: var(--success-color);
}

.data-card[data-type="warning"] .data-card-icon {
  background: linear-gradient(135deg, var(--warning-color-light) 0%, var(--warning-color-light-90) 100%);
  color: var(--warning-color);
}

.data-card[data-type="danger"] .data-card-icon {
  background: linear-gradient(135deg, var(--danger-color-light) 0%, var(--danger-color-light-90) 100%);
  color: var(--danger-color);
}

.data-card[data-type="info"] .data-card-icon {
  background: linear-gradient(135deg, var(--info-color-light) 0%, var(--info-color-light-90) 100%);
  color: var(--info-color);
}

.data-card-progress {
  margin-top: 16px;
}

.progress-label {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-top: 4px;
  text-align: center;
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

.data-card.animation {
  animation: fadeInUp 0.5s ease-out;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .data-card-content {
    padding: 16px;
  }
  
  .data-card-value {
    font-size: 24px;
  }
  
  .data-card-value.has-trend {
    font-size: 20px;
  }
  
  .data-card-icon {
    width: 52px;
    height: 52px;
  }
  
  .data-card-title {
    font-size: 13px;
  }
  
  .data-card-subtitle {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .data-card-content {
    padding: 12px;
  }
  
  .data-card-value {
    font-size: 20px;
  }
  
  .data-card-value.has-trend {
    font-size: 18px;
  }
  
  .data-card-icon {
    width: 44px;
    height: 44px;
  }
}
</style>