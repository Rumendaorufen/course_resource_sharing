<template>
  <div class="login-container">
    <div class="login-box">
      <h2>课程资源共享平台</h2>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-button"
            @click.prevent="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import service from '@/utils/request' // 引入 axios 实例

const store = useStore()
const router = useRouter()
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = ref({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  console.log('Login attempt with:', loginForm.value)
  
  if (!loginFormRef.value) {
    console.error('Login form ref is null')
    return
  }
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) {
      console.log('Form validation failed')
      return
    }
    
    loading.value = true
    console.log('Starting login request...')
    
    // 构造登录请求参数
    const loginData = {
      username: loginForm.value.username.trim(),
      password: loginForm.value.password
    }
    
    console.log('Sending login request with data:', loginData)
    
    // 发送登录请求
    const response = await service({
      url: '/auth/login',  // 不需要添加/api前缀，因为已经在axios实例中配置了baseURL
      method: 'post',
      data: loginData,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      withCredentials: true  
    })
    
    console.log('Login response:', response)
    
    const { code, data, message } = response.data || {}
    if (code === 200 && data) {
      // 存储认证信息
      await store.commit('SET_TOKEN', data.token)
      await store.commit('SET_USER', data.user)  
      
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      throw new Error(message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    const errorMsg = error.response?.data?.message || error.message || '登录失败，请检查用户名和密码'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #409EFF;
}

.login-button {
  width: 100%;
}
</style>
