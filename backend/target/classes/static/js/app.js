const app = Vue.createApp({
    data() {
        return {
            isAuthenticated: false,
            isRegister: false,
            username: '',
            userRole: '',
            currentView: 'courses',
            message: '',
            loginForm: {
                username: '',
                password: ''
            },
            registerForm: {
                username: '',
                password: '',
                realName: '',
                email: '',
                role: 'STUDENT'
            }
        }
    },
    mounted() {
        // 检查是否已登录
        const token = localStorage.getItem('token');
        if (token) {
            this.checkAuth();
        }

        // 初始化Bootstrap的Toast组件
        this.toast = new bootstrap.Toast(this.$refs.toast);
    },
    methods: {
        async checkAuth() {
            try {
                const response = await apiService.user.getCurrentUser();
                this.isAuthenticated = true;
                this.username = response.data.username;
                this.userRole = response.data.role;
            } catch (error) {
                this.logout();
            }
        },
        async login() {
            try {
                const response = await apiService.auth.login(this.loginForm);
                localStorage.setItem('token', response.data.token);
                await this.checkAuth();
                this.showMessage('登录成功');
            } catch (error) {
                this.showMessage(error.message || '登录失败');
            }
        },
        async register() {
            try {
                await apiService.auth.register(this.registerForm);
                this.isRegister = false;
                this.showMessage('注册成功，请登录');
            } catch (error) {
                this.showMessage(error.message || '注册失败');
            }
        },
        logout() {
            localStorage.removeItem('token');
            this.isAuthenticated = false;
            this.username = '';
            this.userRole = '';
            this.currentView = 'courses';
        },
        showMessage(msg) {
            this.message = msg;
            this.toast.show();
        }
    }
});

// 注册组件
app.component('courses', CoursesComponent);
app.component('resources', ResourcesComponent);
app.component('homework', HomeworkComponent);
app.component('submissions', SubmissionsComponent);

// 挂载应用
app.mount('#app');
