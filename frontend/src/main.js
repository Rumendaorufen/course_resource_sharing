import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 引入样式
import 'element-plus/dist/index.css'
import './assets/main.css'

// 引入主题管理器
import { initTheme, watchSystemTheme } from './utils/theme'

import App from './App.vue'
import router from './router'
import store from './store'

// 初始化主题
initTheme()

// 监听系统主题变化
const unwatchSystemTheme = watchSystemTheme()

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('Global error:', err)
  console.error('Error info:', info)
}

// 全局属性
app.config.globalProperties.$theme = {
  initTheme,
  watchSystemTheme: unwatchSystemTheme
}

// 挂载插件
app.use(store)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  // 自定义配置
  size: 'default',
  zIndex: 3000
})

// 等待路由准备就绪后再挂载应用
router.isReady().then(() => {
  app.mount('#app')
})
