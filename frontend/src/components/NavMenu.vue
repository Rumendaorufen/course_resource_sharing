<template>
  <el-menu
    :router="true"
    mode="horizontal"
    :default-active="activeIndex"
  >
    <el-menu-item index="/">首页</el-menu-item>
    <el-menu-item index="/courses">课程管理</el-menu-item>
    <el-menu-item index="/resources">资源管理</el-menu-item>
    <el-menu-item index="/homework">作业管理</el-menu-item>
    <el-menu-item index="/studentManagement" v-if="userRole === 'ADMIN' || userRole === 'TEACHER'">学生管理</el-menu-item>
    
    <div class="flex-grow" />
    
    <template v-if="isAuthenticated">
      <el-dropdown>
        <span class="el-dropdown-link">
          {{ currentUser?.username }}
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </template>
    <template v-else>
      <el-menu-item index="/login">登录</el-menu-item>
    </template>
  </el-menu>
</template>

<script>
import { defineComponent, computed, ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'NavMenu',
  components: {
    ArrowDown
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    
    const activeIndex = computed(() => route.path)
    const isAuthenticated = computed(() => store.getters.isAuthenticated)
    const currentUser = computed(() => store.getters.currentUser)
    const userRole = computed(() => store.getters.user?.role)

    const handleLogout = () => {
      store.commit('logout')
      router.push('/login')
    }

    return {
      activeIndex,
      isAuthenticated,
      currentUser,
      userRole,
      handleLogout
    }
  }
})
</script>

<style scoped>
.flex-grow {
  flex-grow: 1;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: var(--el-text-color-primary);
  margin: 0 20px;
}
</style>
}
</style>
