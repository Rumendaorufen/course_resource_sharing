<template>
  <el-dropdown trigger="click" @command="handleThemeChange">
    <div class="theme-toggle-wrapper">
      <el-icon :size="20">
        <component :is="currentThemeIcon" />
      </el-icon>
      <span class="theme-text">{{ currentThemeText }}</span>
      <el-icon class="el-icon--right" :size="16">
        <ArrowDown />
      </el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="light">
          <el-icon :size="16" class="mr-2"><Sunny /></el-icon>
          浅色主题
        </el-dropdown-item>
        <el-dropdown-item command="dark">
          <el-icon :size="16" class="mr-2"><Moon /></el-icon>
          深色主题
        </el-dropdown-item>
        <el-dropdown-item command="system">
          <el-icon :size="16" class="mr-2"><Monitor /></el-icon>
          跟随系统
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { toggleTheme, getCurrentTheme, setTheme, ThemeType } from '../utils/theme'
import { ArrowDown, Sunny, Moon, Monitor } from '@element-plus/icons-vue'

export default {
  name: 'ThemeToggle',
  components: {
    ArrowDown,
    Sunny,
    Moon,
    Monitor
  },
  setup() {
    const theme = ref(getCurrentTheme())
    
    // 当前主题图标
    const currentThemeIcon = computed(() => {
      return theme.value === ThemeType.DARK ? Moon : Sunny
    })
    
    // 当前主题文本
    const currentThemeText = computed(() => {
      return theme.value === ThemeType.DARK ? '深色' : '浅色'
    })
    
    // 处理主题切换
    const handleThemeChange = (command) => {
      if (command === 'system') {
        // 移除本地存储的主题，让系统自动检测
        localStorage.removeItem('app_theme')
        // 重新检测系统主题
        const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
        setTheme(prefersDark ? ThemeType.DARK : ThemeType.LIGHT)
        theme.value = getCurrentTheme()
      } else {
        setTheme(command)
        theme.value = command
      }
    }
    
    // 监听主题变化事件
    const handleThemeChanged = (event) => {
      theme.value = event.detail.theme
    }
    
    onMounted(() => {
      window.addEventListener('themeChanged', handleThemeChanged)
    })
    
    // 组件卸载时清理事件监听
    const onUnmounted = () => {
      window.removeEventListener('themeChanged', handleThemeChanged)
    }
    
    return {
      theme,
      currentThemeIcon,
      currentThemeText,
      handleThemeChange,
      onUnmounted
    }
  }
}
</script>

<style scoped>
.theme-toggle-wrapper {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: var(--border-radius-base);
  transition: background-color var(--transition-base);
}

.theme-toggle-wrapper:hover {
  background-color: var(--background-hover);
}

.theme-text {
  margin: 0 4px;
  font-size: 14px;
}

.mr-2 {
  margin-right: 8px;
}
</style>