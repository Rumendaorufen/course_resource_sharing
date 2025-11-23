/**
 * 主题管理器
 * 提供主题切换和管理功能
 */

// 主题类型定义
export const ThemeType = {
  LIGHT: 'light',
  DARK: 'dark'
}

// 本地存储键名
const THEME_STORAGE_KEY = 'app_theme'

/**
 * 获取当前主题
 * @returns {string} 当前主题类型
 */
export const getCurrentTheme = () => {
  return localStorage.getItem(THEME_STORAGE_KEY) || ThemeType.LIGHT
}

/**
 * 设置主题
 * @param {string} theme - 主题类型
 */
export const setTheme = (theme) => {
  if (!Object.values(ThemeType).includes(theme)) {
    console.warn(`Invalid theme type: ${theme}, using default light theme`)
    theme = ThemeType.LIGHT
  }
  
  // 保存到本地存储
  localStorage.setItem(THEME_STORAGE_KEY, theme)
  
  // 更新HTML类名
  if (theme === ThemeType.DARK) {
    document.documentElement.classList.add('dark-theme')
  } else {
    document.documentElement.classList.remove('dark-theme')
  }
  
  // 触发主题变更事件
  window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme } }))
}

/**
 * 切换主题
 */
export const toggleTheme = () => {
  const currentTheme = getCurrentTheme()
  const newTheme = currentTheme === ThemeType.LIGHT ? ThemeType.DARK : ThemeType.LIGHT
  setTheme(newTheme)
  return newTheme
}

/**
 * 初始化主题
 */
export const initTheme = () => {
  // 尝试从系统偏好获取主题
  const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
  
  // 如果本地存储没有主题，则使用系统偏好
  if (!localStorage.getItem(THEME_STORAGE_KEY)) {
    setTheme(prefersDark ? ThemeType.DARK : ThemeType.LIGHT)
  } else {
    // 否则使用本地存储的主题
    setTheme(getCurrentTheme())
  }
}

/**
 * 监听系统主题变化
 */
export const watchSystemTheme = () => {
  if (window.matchMedia) {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    const handleChange = (e) => {
      // 只有在未手动设置主题时才跟随系统变化
      if (!localStorage.getItem(THEME_STORAGE_KEY)) {
        setTheme(e.matches ? ThemeType.DARK : ThemeType.LIGHT)
      }
    }
    
    mediaQuery.addEventListener('change', handleChange)
    
    // 返回清理函数
    return () => {
      mediaQuery.removeEventListener('change', handleChange)
    }
  }
  
  return () => {}
}