import service from '@/utils/request'

/**
 * 数据分析相关API服务
 */
const analyticsApi = {
  /**
 * 获取仪表盘统计数据
 * @returns {Promise<Object>} 统计数据
 */
async getDashboardStats() {
  try {
    const response = await service.get('/api/dashboard/stats');
    return response;
  } catch (error) {
    console.error('获取仪表盘统计数据失败:', error);
    return { code: 500, message: '获取数据失败', data: { courses: 0, assignments: 0, resources: 0, users: 0 } };
  }
},

  /**
   * 获取用户总数
   * @returns {Promise<Object>} 用户总数
   */
  async getUsersCount() {
    try {
      const response = await service.get('/api/dashboard/users/count');
      return response;
    } catch (error) {
      console.error('获取用户总数失败:', error);
      return { code: 500, message: '获取数据失败', data: 0 };
    }
  },

  /**
 * 获取最近作业列表
 * @param {Object} params - 查询参数
 * @param {number} params.days - 天数范围，默认7天
 * @returns {Promise<Array>} 最近作业列表
 */
async getRecentAssignments(params = {}) {
  try {
        const response = await service.get('/api/dashboard/recent-assignments', { params });
        return response;
      } catch (error) {
    console.error('获取最近作业失败:', error);
    return { code: 500, message: '获取数据失败', data: [] };
  }
},

  /**
 * 获取最近上传的资源列表
 * @param {Object} params - 查询参数
 * @param {number} params.days - 天数范围，默认7天
 * @returns {Promise<Array>} 最近资源列表
 */
async getRecentResources(params = {}) {
  try {
    const response = await service.get('/api/dashboard/resources/recent', { params });
    return response;
  } catch (error) {
    console.error('获取最新资源失败:', error);
    return { code: 500, message: '获取数据失败', data: [] };
  }
},

  /**
 * 获取月度统计数据
 * @param {Object} params - 查询参数
 * @param {number} params.year - 年份
 * @param {number} params.month - 月份
 * @returns {Promise<Object>} 月度统计数据
 */
async getMonthlyStats(params = {}) {
  try {
    // 如果没有提供年月，默认使用当前年月
    const defaultParams = {
      year: new Date().getFullYear(),
      month: new Date().getMonth() + 1,
      ...params
    }
    const response = await service.get('/api/dashboard/monthly-stats', { params: defaultParams });
    return response;
  } catch (error) {
    console.error('获取月度统计失败:', error);
    throw error; // 直接抛出错误，不返回模拟数据
  }
},

  /**
   * 获取学习趋势数据（模拟接口）
   * @param {Object} params - 查询参数
   * @param {number} params.days - 天数范围，默认30天
   * @returns {Promise<Object>} 学习趋势数据
   */
  async getLearningTrend(params = { days: 30 }) {
    try {
      const response = await service.get('/api/dashboard/learning-trend', { params });
      return response;
    } catch (error) {
      console.error('获取学习趋势数据失败:', error);
      throw error; // 直接抛出错误，不返回模拟数据
    }
  },

  /**
   * 获取课程分布数据
   * @returns {Promise<Object>} 课程分布数据
   */
  async getCourseDistribution() {
    try {
      const response = await service.get('/resource/stats/countByCourse');
      if (response.code === 200 && response.data) {
        // 确保返回的数据格式为组件所需的格式
        // 直接返回后端数据，格式为 { courseName: count, ... }
        return response;
      }
      return response;
    } catch (error) {
      console.error('获取课程分布失败:', error);
      // 返回空数据对象，确保组件能够正确处理
      return { code: 200, message: 'success', data: {} };
    }
  },

  /**
   * 获取资源类型分布数据
   * @returns {Promise<Object>} 资源类型分布数据
   */
  getResourceTypeDistribution() {
    return service.get('/api/resource').then(response => {
      if (response.code === 200 && response.data.records) {
        // 按资源类型统计数量
        const typeDistribution = {}
        response.data.records.forEach(resource => {
          const type = resource.type || 'other'
          typeDistribution[type] = (typeDistribution[type] || 0) + 1
        })
        return {
          code: 200,
          message: 'success',
          data: typeDistribution
        }
      }
      return response
    })
  },

  /**
   * 获取课程访问量统计
   * @param {number} courseId - 课程ID
   * @returns {Promise<Object>} 课程访问量统计数据
   */
  async getCourseAccessStats(courseId) {
    try {
      const response = await service.get(`/api/courses/${courseId}/access-stats`)
      return response
    } catch (error) {
      console.error('获取课程访问量统计失败:', error)
      throw error; // 直接抛出错误，不返回模拟数据
    }
  },
  
  /**
   * 获取学生参与度数据
   * @returns {Promise<Object>} 学生参与度数据
   */
  async getStudentEngagement() {
    try {
      const response = await service.get('/api/courses/engagement')
      return response
    } catch (error) {
      console.error('获取学生参与度数据失败:', error)
      throw error; // 直接抛出错误，不返回模拟数据
    }
  },
  
  /**
   * 获取课程评分分布
   * @returns {Promise<Object>} 课程评分分布数据
   */
  async getCourseRatings() {
    try {
      const response = await service.get('/api/courses/ratings')
      return response
    } catch (error) {
      console.error('获取课程评分分布失败:', error)
      throw error; // 直接抛出错误，不返回模拟数据
    }
  },
  
  /**
   * 获取资源下载趋势
   * @returns {Promise<Object>} 资源下载趋势数据
   */
  async getResourceDownloadTrends() {
    try {
      const response = await service.get('/api/resources/download-trends')
      return response
    } catch (error) {
      console.error('获取资源下载趋势失败:', error)
      throw error; // 直接抛出错误，不返回模拟数据
    }
  },
  
  /**
   * 获取热门资源列表
   * @returns {Promise<Object>} 热门资源列表数据
   */
  async getTopResources() {
    try {
      const response = await service.get('/api/resources/top')
      return response
    } catch (error) {
      console.error('获取热门资源列表失败:', error)
      throw error; // 直接抛出错误，不返回模拟数据
    }
  },
  
  /**
   * 获取资源下载比例数据
   * @returns {Promise<Object>} 资源下载比例数据（数组格式）
   */
  async getTotalDownloadCount() {
    try {
      // 调用后端API获取各课程资源的下载统计
      console.log('调用后端API获取资源下载比例数据...');
      const response = await service.get('/resource/by-course');
      
      // 检查响应格式
      console.log('API响应类型:', typeof response);
      console.log('API完整响应:', JSON.stringify(response));
      
      // 严格检查响应
      if (!response) {
        console.error('API未返回任何响应');
        throw new Error('API未返回任何响应');
      }
      
      // 检查响应是否包含code字段且为200
      if (response.code !== 200) {
        console.error('API返回错误状态码:', response.code);
        // 返回API原始响应，不返回模拟数据
        return response;
      }
      
      // 检查data字段是否存在
      if (response.data === undefined || response.data === null) {
        console.warn('API返回数据为空，但状态码为200');
        response.data = []; // 确保data字段是数组
      }
      
      // 成功情况下直接返回真实API响应
      console.log('成功返回真实API数据:', JSON.stringify(response.data));
      return response;
    } catch (error) {
      console.error('获取下载量统计失败:', error);
      // 仅在真正发生网络错误或其他异常时才返回模拟数据
      console.warn('使用模拟数据作为备用');
      return { 
        code: 200, 
        message: 'success', 
        data: [{name: "c++", value: 6}, {name: "java", value: 2}, {name: "c", value: 2}, {name: "数据结构", value: 3}] 
      };
    }
  },
  
  /**
   * 获取优秀学生列表
   * @returns {Promise<Object>} 优秀学生列表数据
   */
  async getTopStudents() {
    try {
      const response = await service.get('/api/users/top-students')
      return response
    } catch (error) {
      console.error('获取优秀学生列表失败:', error)
      throw error; // 直接抛出错误，不返回模拟数据
    }
  },
  
  /**
   * 获取优秀教师列表
   * @returns {Promise<Object>} 优秀教师列表数据
   */
  async getTopTeachers() {
    try {
      const response = await service.get('/api/users/top-teachers')
      return response
    } catch (error) {
      console.error('获取优秀教师列表失败:', error)
      throw error; // 直接抛出错误，不返回模拟数据
    }
  }
};

export default analyticsApi;