import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import store from '@/store'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',  // 使用后端配置的 context-path
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json, application/octet-stream'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    console.log('Request URL:', config.baseURL + config.url)
    console.log('Request Method:', config.method)
    console.log('Request Headers:', config.headers)
    
    // 添加认证 Token
    const token = store.getters.token
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 如果是文件下载请求，修改responseType和headers
    if (config.responseType === 'blob') {
      config.headers['Accept'] = 'application/octet-stream'
    }
    
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('Response:', response)
    
    // 如果是文件下载响应，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    return response
  },
  async error => {
    console.error('Response error:', error)
    
    // 处理文件下载错误
    if (error.response?.config?.responseType === 'blob') {
      try {
        const errorBlob = error.response.data
        const errorText = await new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => resolve(reader.result)
          reader.onerror = reject
          reader.readAsText(errorBlob)
        })
        const errorData = JSON.parse(errorText)
        error.response.data = errorData
      } catch (e) {
        console.error('Error parsing blob error response:', e)
      }
    }
    
    if (!error.response) {
      ElMessage.error('网络错误，请检查服务器连接')
    } else {
      const { status, data } = error.response
      if (status === 401) {
        store.dispatch('logout').then(() => {
          router.push('/login')
        })
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error(data?.message || '没有权限访问')
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    }
    return Promise.reject(error)
  }
)

export default service
