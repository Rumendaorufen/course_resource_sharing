import request from '@/utils/request'

const state = {
  assignments: [],
  currentAssignment: null,
  loading: false
}

const mutations = {
  SET_ASSIGNMENTS(state, assignments) {
    state.assignments = assignments
  },
  SET_CURRENT_ASSIGNMENT(state, assignment) {
    state.currentAssignment = assignment
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  }
}

const actions = {
  // 获取学生的作业列表
  async getStudentAssignments({ commit }, studentId) {
    commit('SET_LOADING', true)
    try {
      const { data } = await request.get(`/assignments/student/${studentId}`)
      commit('SET_ASSIGNMENTS', data)
      return data
    } catch (error) {
      console.error('Failed to get student assignments:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 提交作业
  async submitAssignment({ commit }, { assignmentId, submission }) {
    try {
      const response = await request.post(`/assignments/student/${assignmentId}/submit`, submission)
      return response.data
    } catch (error) {
      console.error('Failed to submit assignment:', error)
      throw error
    }
  },

  // 获取作业提交详情
  async getSubmissionDetail({ commit }, submissionId) {
    try {
      const { data } = await request.get(`/assignments/student/submission/${submissionId}`)
      return data
    } catch (error) {
      console.error('Failed to get submission detail:', error)
      throw error
    }
  }
}

const getters = {
  assignmentList: state => state.assignments,
  currentAssignment: state => state.currentAssignment,
  isLoading: state => state.loading
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
