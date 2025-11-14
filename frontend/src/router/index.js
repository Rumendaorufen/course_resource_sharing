import { createRouter, createWebHashHistory } from 'vue-router'
import store from '../store'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/Home.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'userManagement',
        name: 'userManagement',
        component: () => import('../views/UserManagement.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'studentManagement',
        name: 'StudentManagement',
        component: () => import('../views/StudentManagement.vue'),
        meta: { roles: ['TEACHER'] }
      },
     
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('../views/Courses.vue'),
        meta: { roles: ['ADMIN'] }
      },

      {
        path: 'resources',
        name: 'Resources',
        component: () => import('../views/Resources.vue'),
        meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'assignments',
        name: 'Assignments',
        component: () => import('../views/Assignments.vue'),
        meta: { roles: ['TEACHER', 'STUDENT'] }
      },
      {
        path: 'assignment-submissions/:id',
        name: 'AssignmentSubmissions',
        component: () => import('../views/AssignmentSubmissions.vue'),
        meta: { roles: ['TEACHER'] }
      },
      {
        path: 'student-assignments',
        name: 'StudentAssignments',
        component: () => import('../views/StudentAssignments.vue'),
        meta: { roles: ['STUDENT'] }
      },
      {
        path: 'homework-submission/:id',
        name: 'HomeworkSubmission',
        component: () => import('../views/HomeworkSubmission.vue'),
        meta: { roles: ['STUDENT'] }
      },
      {
        path: 'graded-assignment-detail/:id',
        name: 'GradedAssignmentDetail',
        component: () => import('../views/GradedAssignmentDetail.vue'),
        meta: { roles: ['STUDENT'] }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(),  // 使用 hash 模式
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters.isAuthenticated
  const userRole = store.getters.user?.role

  // 检查是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      next('/login')
      return
    }

    // 检查角色权限
    const requiresRole = to.matched.some(record => 
      record.meta.roles && !record.meta.roles.includes(userRole)
    )
    
    if (requiresRole) {
      next('/dashboard')
      return
    }
  }

  // 如果已登录，访问登录页则重定向到首页
  if (to.path === '/login' && isAuthenticated) {
    next('/dashboard')
    return
  }

  next()
})

// 导航错误处理
router.onError((error) => {
  console.error('Navigation error:', error)
})

export default router
