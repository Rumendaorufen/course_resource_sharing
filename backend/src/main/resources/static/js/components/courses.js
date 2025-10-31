const CoursesComponent = {
    template: `
        <div class="courses">
            <!-- 教师创建课程按钮 -->
            <div class="d-flex justify-content-between align-items-center mb-4" v-if="userRole === 'TEACHER'">
                <h2>我的课程</h2>
                <button class="btn btn-primary" @click="showCreateModal">
                    <i class="mdi mdi-plus"></i> 创建课程
                </button>
            </div>
            <h2 v-else>课程列表</h2>

            <!-- 课程列表 -->
            <div class="row">
                <div class="col-md-4 mb-4" v-for="course in courses" :key="course.id">
                    <div class="card h-100">
                        <div class="card-body">
                            <h5 class="card-title">{{ course.name }}</h5>
                            <p class="card-text">{{ course.description }}</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="text-muted">教师：{{ course.teacherName }}</span>
                                <button v-if="userRole === 'STUDENT' && !course.enrolled"
                                        class="btn btn-outline-primary btn-sm"
                                        @click="enrollCourse(course.id)">
                                    选课
                                </button>
                                <button v-else-if="userRole === 'STUDENT' && course.enrolled"
                                        class="btn btn-outline-success btn-sm" disabled>
                                    已选课
                                </button>
                                <div v-else-if="userRole === 'TEACHER'">
                                    <button class="btn btn-outline-primary btn-sm me-2"
                                            @click="editCourse(course)">
                                        编辑
                                    </button>
                                    <button class="btn btn-outline-danger btn-sm"
                                            @click="deleteCourse(course.id)">
                                        删除
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 创建/编辑课程模态框 -->
            <div class="modal fade" ref="courseModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">{{ isEdit ? '编辑课程' : '创建课程' }}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <form @submit.prevent="saveCourse">
                                <div class="mb-3">
                                    <label class="form-label">课程名称</label>
                                    <input type="text" class="form-control" v-model="courseForm.name" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">课程描述</label>
                                    <textarea class="form-control" v-model="courseForm.description" rows="3" required></textarea>
                                </div>
                                <div class="text-end">
                                    <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">取消</button>
                                    <button type="submit" class="btn btn-primary">保存</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    props: {
        userRole: String
    },
    data() {
        return {
            courses: [],
            isEdit: false,
            courseForm: {
                id: null,
                name: '',
                description: ''
            },
            modal: null
        }
    },
    mounted() {
        this.loadCourses();
        this.modal = new bootstrap.Modal(this.$refs.courseModal);
    },
    methods: {
        async loadCourses() {
            try {
                const response = await apiService.course.list();
                this.courses = response.data;
            } catch (error) {
                this.$emit('show-message', error.message || '加载课程失败');
            }
        },
        showCreateModal() {
            this.isEdit = false;
            this.courseForm = {
                id: null,
                name: '',
                description: ''
            };
            this.modal.show();
        },
        editCourse(course) {
            this.isEdit = true;
            this.courseForm = { ...course };
            this.modal.show();
        },
        async saveCourse() {
            try {
                if (this.isEdit) {
                    await apiService.course.update(this.courseForm.id, this.courseForm);
                } else {
                    await apiService.course.create(this.courseForm);
                }
                this.modal.hide();
                this.loadCourses();
                this.$emit('show-message', `${this.isEdit ? '更新' : '创建'}课程成功`);
            } catch (error) {
                this.$emit('show-message', error.message || `${this.isEdit ? '更新' : '创建'}课程失败`);
            }
        },
        async deleteCourse(id) {
            if (!confirm('确定要删除这个课程吗？')) return;
            
            try {
                await apiService.course.delete(id);
                this.loadCourses();
                this.$emit('show-message', '删除课程成功');
            } catch (error) {
                this.$emit('show-message', error.message || '删除课程失败');
            }
        },
        async enrollCourse(courseId) {
            try {
                await apiService.course.enroll(courseId);
                this.loadCourses();
                this.$emit('show-message', '选课成功');
            } catch (error) {
                this.$emit('show-message', error.message || '选课失败');
            }
        }
    }
};
