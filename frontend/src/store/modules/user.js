import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const state = {
  token: localStorage.getItem('token'),
  userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
  },
  SET_USER_INFO: (state, userInfo) => {
    state.userInfo = userInfo
    if (userInfo) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      localStorage.removeItem('userInfo')
    }
  }
}

const actions = {
  // 登录
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      request({
        url: '/auth/login',
        method: 'post',
        data: { username, password }
      })
        .then(response => {
          const { data } = response
          if (data.code === 200 && data.data) {
            const { token, userInfo } = data.data
            commit('SET_TOKEN', token)
            commit('SET_USER_INFO', userInfo)
            resolve(true)
          } else {
            reject(new Error(data.message || '登录失败'))
          }
        })
        .catch(error => {
          console.error('Login error:', error)
          reject(error)
        })
    })
  },

  // 登出
  logout({ commit }) {
    commit('SET_TOKEN', '')
    commit('SET_USER_INFO', null)
    return Promise.resolve()
  },

  // 重置 token
  resetToken({ commit }) {
    commit('SET_TOKEN', '')
    commit('SET_USER_INFO', null)
    return Promise.resolve()
  }
}

const getters = {
  token: state => state.token,
  userInfo: state => state.userInfo
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
