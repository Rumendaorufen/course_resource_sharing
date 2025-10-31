import { createStore } from 'vuex'
import service from '../utils/request'
import assignment from './modules/assignment'

export default createStore({
  modules: {
    assignment
  },
  state: {
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || '{}')
  },
  
  getters: {
    token: state => state.token,
    user: state => state.user,
    isAuthenticated: state => !!state.token
  },
  
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    SET_USER(state, user) {
      state.user = user
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
    },
    clearUserInfo(state) {
      state.token = ''
      state.user = {}
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  },
  
  actions: {
    async login({ commit }, credentials) {
      try {
        console.log('Sending login request...')
        const response = await service.post('/auth/login', credentials)
        console.log('Login response:', response)

        const { data } = response
        if (data.code === 200 && data.data) {
          const { token, user } = data.data
          commit('SET_TOKEN', token)
          commit('SET_USER', user)
          return true
        }
        return false
      } catch (error) {
        console.error('Login error:', error)
        throw error
      }
    },

    // 退出登录
    logout({ commit }) {
      try {
        commit('clearUserInfo')
        return true
      } catch (error) {
        console.error('Logout error:', error)
        return false
      }
    }
  }
})
