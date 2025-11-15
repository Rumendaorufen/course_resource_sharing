// API请求配置
const api = axios.create({
    baseURL: '/api',
    timeout: 5000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// 请求拦截器
api.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
api.interceptors.response.use(
    response => {
        return response.data;
    },
    error => {
        if (error.response) {
            if (error.response.status === 401) {
                // 未授权，清除token并跳转到登录页
                localStorage.removeItem('token');
                window.location.reload();
            }
            return Promise.reject(error.response.data);
        }
        return Promise.reject(error);
    }
);

// API方法
const apiService = {
    // 认证相关
    auth: {
        login: (data) => api.post('/auth/login', data),
        register: (data) => api.post('/auth/register', data)
    },

    // 用户相关
    user: {
        getCurrentUser: () => api.get('/user/current'),
        updateProfile: (data) => api.put('/user/profile', data),
        updatePassword: (data) => api.put('/user/password', data)
    },

    // 课程相关
    course: {
        list: () => api.get('/course'),
        create: (data) => api.post('/course', data),
        update: (id, data) => api.put(`/course/${id}`, data),
        delete: (id) => api.delete(`/course/${id}`),
        getById: (id) => api.get(`/course/${id}`),
        enroll: (courseId) => api.post(`/course/${courseId}/enroll`),
        listEnrolled: () => api.get('/course/enrolled')
    },

    // 资源相关
    resource: {
        list: (courseId) => api.get(`/resource?courseId=${courseId}`),
        upload: (courseId, formData) => api.post(`/resource?courseId=${courseId}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        }),
        delete: (id) => api.delete(`/resource/${id}`),
        download: (id) => api.get(`/resource/${id}/download`, { responseType: 'blob' })
    },

    // 作业相关
    homework: {
        list: (courseId) => api.get(`/homework?courseId=${courseId}`),
        create: (data) => api.post('/homework', data),
        update: (id, data) => api.put(`/homework/${id}`, data),
        delete: (id) => api.delete(`/homework/${id}`),
        getById: (id) => api.get(`/homework/${id}`)
    },

    // 作业提交相关
    submission: {
        list: (homeworkId) => api.get(`/homework-submission?homeworkId=${homeworkId}`),
        submit: (homeworkId, formData) => api.post(`/homework-submission?homeworkId=${homeworkId}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        }),
        grade: (id, data) => api.put(`/homework-submission/${id}/grade`, data),
        getMySubmissions: () => api.get('/homework-submission/my')
    }
};
