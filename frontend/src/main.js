import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 引入样式
import 'element-plus/dist/index.css'
import './assets/main.css'

import App from './App.vue'
import router from './router'
import store from './store'

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

app.use(store)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 等待路由准备就绪后再挂载应用
router.isReady().then(() => {
  app.mount('#app')
})
