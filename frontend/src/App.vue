<template>
  <router-view />
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeFilled,
  Reading,
  User,
  EditPen,
  Files,
  UserFilled,
  SwitchButton
} from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()
const route = useRoute()

// 计算属性
const isAuthenticated = computed(() => store.getters.isAuthenticated)
const userInfo = computed(() => store.getters.user || {})
const userRole = computed(() => userInfo.value?.role)
const userAvatar = computed(() => userInfo.value?.avatar)
const activeMenu = computed(() => route.path)

// 处理下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      await store.dispatch('logout')
      router.push('/login')
      break
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
}

#app {
  height: 100%;
  width: 100%;
}
</style>
