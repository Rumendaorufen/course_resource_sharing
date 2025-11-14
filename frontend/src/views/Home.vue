<template>
  <el-container class="home-container">
    <!-- 侧边栏 -->
    <el-aside width="200px">
      <div class="logo-container">
        <h1>课程资源平台</h1>
      </div>
      <el-menu
        :router="true"
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/userManagement" v-if="isAdmin">
          <el-icon><Management /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/courses" v-if="isAdmin">
          <el-icon><Management /></el-icon>
          <span>课程管理</span>
        </el-menu-item>

        <el-menu-item index="/resources">
          <el-icon><Folder /></el-icon>
          <span>资源中心</span>
        </el-menu-item>

        <el-menu-item
          v-if="user.role === 'TEACHER'"
          index="/assignments"
          :route="{ path: '/assignments' }"
        >
          <el-icon><Document /></el-icon>
          <span>作业管理</span>
        </el-menu-item>

        <el-menu-item
          v-if="user.role === 'STUDENT'"
          index="/student-assignments"
          :route="{ path: '/student-assignments' }"
        >
          <el-icon><Document /></el-icon>
          <span>我的作业</span>
        </el-menu-item>

        <el-menu-item
          v-if="user.role === 'TEACHER'"
          index="/studentManagement"
          :route="{ path: '/studentManagement' }"
        >
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主要内容区域 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeFilled,
  Management,
  Folder,
  Document,
  ArrowDown,
  User
} from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()
const route = useRoute()

const username = computed(() => store.state.user?.username || '未登录')
const isAdmin = computed(() => store.state.user?.role === 'ADMIN')
const user = computed(() => store.state.user)
const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
  if (command === 'logout') {
    await store.dispatch('logout')
    router.push('/login')
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #4a5064;
}

.logo-container h1 {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

.el-menu-vertical {
  border-right: none;
  height: calc(100vh - 60px);
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #303133;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

:deep(.el-menu-item) {
  display: flex;
  align-items: center;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 8px;
  font-size: 18px;
}
</style>
