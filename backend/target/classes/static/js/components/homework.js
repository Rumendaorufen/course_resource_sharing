const HomeworkComponent = {
    template: `
        <div class="homework">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>作业管理</h2>
                <div>
                    <select class="form-select d-inline-block w-auto me-2" v-model="selectedCourseId" @change="loadHomework">
                        <option value="">选择课程</option>
                        <option v-for="course in courses" :key="course.id" :value="course.id">
                            {{ course.name }}
                        </option>
                    </select>
                    <button class="btn btn-primary" @click="showCreateModal" :disabled="!selectedCourseId">
                        <i class="mdi mdi-plus"></i> 发布作业
                    </button>
                </div>
            </div>

            <!-- 作业列表 -->
            <div class="card" v-if="selectedCourseId">
                <div class="card-body">
                    <div v-if="homeworkList.length === 0" class="text-center py-4">
                        <p class="text-muted">暂无作业</p>
                    </div>
                    <div v-else>
                        <div class="list-group">
                            <div class="list-group-item" v-for="homework in homeworkList" :key="homework.id">
                                <div class="d-flex w-100 justify-content-between">
                                    <h5 class="mb-1">{{ homework.title }}</h5>
                                    <small class="text-muted">截止日期：{{ formatDate(homework.deadline) }}</small>
                                </div>
                                <p class="mb-1">{{ homework.description }}</p>
                                <div class="d-flex justify-content-between align-items-center mt-2">
                                    <small class="text-muted">
                                        提交情况：{{ homework.submissionCount }}/{{ homework.totalStudents }}
                                    </small>
                                    <div>
                                        <button class="btn btn-outline-primary btn-sm me-2"
                                                @click="viewSubmissions(homework)">
                                            查看提交
                                        </button>
                                        <button class="btn btn-outline-secondary btn-sm me-2"
                                                @click="editHomework(homework)">
                                            编辑
                                        </button>
                                        <button class="btn btn-outline-danger btn-sm"
                                                @click="deleteHomework(homework.id)">
                                            删除
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 创建/编辑作业模态框 -->
            <div class="modal fade" ref="homeworkModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">{{ isEdit ? '编辑作业' : '发布作业' }}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <form @submit.prevent="saveHomework">
                                <div class="mb-3">
                                    <label class="form-label">标题</label>
                                    <input type="text" class="form-control" v-model="homeworkForm.title" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">描述</label>
                                    <textarea class="form-control" v-model="homeworkForm.description" rows="3" required></textarea>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">截止日期</label>
                                    <input type="datetime-local" class="form-control" v-model="homeworkForm.deadline" required>
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

            <!-- 查看提交模态框 -->
            <div class="modal fade" ref="submissionsModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">作业提交情况</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>学生</th>
                                            <th>提交时间</th>
                                            <th>文件</th>
                                            <th>分数</th>
                                            <th>操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="submission in submissions" :key="submission.id">
                                            <td>{{ submission.studentName }}</td>
                                            <td>{{ formatDate(submission.submitTime) }}</td>
                                            <td>
                                                <a href="#" @click.prevent="downloadSubmission(submission)">
                                                    {{ submission.fileName }}
                                                </a>
                                            </td>
                                            <td>
                                                <input type="number" class="form-control form-control-sm w-75"
                                                       v-model="submission.score"
                                                       @change="gradeSubmission(submission)"
                                                       min="0" max="100">
                                            </td>
                                            <td>
                                                <button class="btn btn-outline-primary btn-sm"
                                                        @click="downloadSubmission(submission)">
                                                    下载
                                                </button>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
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
            homeworkList: [],
            submissions: [],
            selectedCourseId: '',
            isEdit: false,
            homeworkForm: {
                id: null,
                title: '',
                description: '',
                deadline: ''
            },
            homeworkModal: null,
            submissionsModal: null
        }
    },
    mounted() {
        this.loadCourses();
        this.homeworkModal = new bootstrap.Modal(this.$refs.homeworkModal);
        this.submissionsModal = new bootstrap.Modal(this.$refs.submissionsModal);
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
        async loadHomework() {
            if (!this.selectedCourseId) {
                this.homeworkList = [];
                return;
            }
            try {
                const response = await apiService.homework.list(this.selectedCourseId);
                this.homeworkList = response.data;
            } catch (error) {
                this.$emit('show-message', error.message || '加载作业失败');
            }
        },
        showCreateModal() {
            this.isEdit = false;
            this.homeworkForm = {
                id: null,
                title: '',
                description: '',
                deadline: ''
            };
            this.homeworkModal.show();
        },
        editHomework(homework) {
            this.isEdit = true;
            this.homeworkForm = { ...homework };
            this.homeworkModal.show();
        },
        async saveHomework() {
            try {
                if (this.isEdit) {
                    await apiService.homework.update(this.homeworkForm.id, this.homeworkForm);
                } else {
                    await apiService.homework.create({
                        ...this.homeworkForm,
                        courseId: this.selectedCourseId
                    });
                }
                this.homeworkModal.hide();
                this.loadHomework();
                this.$emit('show-message', `${this.isEdit ? '更新' : '发布'}作业成功`);
            } catch (error) {
                this.$emit('show-message', error.message || `${this.isEdit ? '更新' : '发布'}作业失败`);
            }
        },
        async deleteHomework(id) {
            if (!confirm('确定要删除这个作业吗？')) return;
            
            try {
                await apiService.homework.delete(id);
                this.loadHomework();
                this.$emit('show-message', '删除作业成功');
            } catch (error) {
                this.$emit('show-message', error.message || '删除作业失败');
            }
        },
        async viewSubmissions(homework) {
            try {
                const response = await apiService.submission.list(homework.id);
                this.submissions = response.data;
                this.submissionsModal.show();
            } catch (error) {
                this.$emit('show-message', error.message || '加载提交记录失败');
            }
        },
        async downloadSubmission(submission) {
            try {
                const response = await apiService.submission.download(submission.id);
                const url = window.URL.createObjectURL(new Blob([response]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', submission.fileName);
                document.body.appendChild(link);
                link.click();
                link.remove();
            } catch (error) {
                this.$emit('show-message', error.message || '下载作业失败');
            }
        },
        async gradeSubmission(submission) {
            try {
                await apiService.submission.grade(submission.id, { score: submission.score });
                this.$emit('show-message', '评分成功');
            } catch (error) {
                this.$emit('show-message', error.message || '评分失败');
            }
        },
        formatDate(date) {
            return new Date(date).toLocaleString();
        }
    },
    watch: {
        selectedCourseId() {
            this.loadHomework();
        }
    }
};
