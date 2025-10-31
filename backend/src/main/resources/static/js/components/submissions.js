const SubmissionsComponent = {
    template: `
        <div class="submissions">
            <h2 class="mb-4">我的作业</h2>

            <!-- 作业列表 -->
            <div class="row">
                <div class="col-md-6 mb-4" v-for="homework in homeworkList" :key="homework.id">
                    <div class="card h-100">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h5 class="card-title">{{ homework.title }}</h5>
                                    <h6 class="card-subtitle mb-2 text-muted">{{ homework.courseName }}</h6>
                                </div>
                                <span class="badge" :class="getStatusBadgeClass(homework)">
                                    {{ getStatusText(homework) }}
                                </span>
                            </div>
                            <p class="card-text">{{ homework.description }}</p>
                            <div class="mt-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <small class="text-muted">
                                        截止日期：{{ formatDate(homework.deadline) }}
                                    </small>
                                    <div v-if="homework.submission">
                                        <span class="me-3">得分：{{ homework.submission.score || '未评分' }}</span>
                                        <button class="btn btn-outline-primary btn-sm"
                                                @click="downloadSubmission(homework.submission)">
                                            查看提交
                                        </button>
                                    </div>
                                    <button v-else
                                            class="btn btn-primary btn-sm"
                                            @click="showSubmitModal(homework)"
                                            :disabled="isDeadlinePassed(homework)">
                                        提交作业
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 提交作业模态框 -->
            <div class="modal fade" ref="submitModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">提交作业</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <form @submit.prevent="submitHomework">
                                <div class="mb-3">
                                    <label class="form-label">选择文件</label>
                                    <input type="file" class="form-control" ref="fileInput" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">备注</label>
                                    <textarea class="form-control" v-model="submissionForm.comment" rows="3"></textarea>
                                </div>
                                <div class="text-end">
                                    <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">取消</button>
                                    <button type="submit" class="btn btn-primary" :disabled="submitting">
                                        <span class="spinner-border spinner-border-sm" v-if="submitting"></span>
                                        {{ submitting ? '提交中...' : '提交' }}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            homeworkList: [],
            currentHomework: null,
            submitting: false,
            submissionForm: {
                comment: ''
            },
            modal: null
        }
    },
    mounted() {
        this.loadHomework();
        this.modal = new bootstrap.Modal(this.$refs.submitModal);
    },
    methods: {
        async loadHomework() {
            try {
                const response = await apiService.homework.listForStudent();
                this.homeworkList = response.data;
            } catch (error) {
                this.$emit('show-message', error.message || '加载作业失败');
            }
        },
        showSubmitModal(homework) {
            this.currentHomework = homework;
            this.submissionForm.comment = '';
            this.modal.show();
        },
        async submitHomework() {
            const file = this.$refs.fileInput.files[0];
            if (!file) return;

            const formData = new FormData();
            formData.append('file', file);
            formData.append('comment', this.submissionForm.comment);

            this.submitting = true;
            try {
                await apiService.submission.submit(this.currentHomework.id, formData);
                this.modal.hide();
                this.loadHomework();
                this.$emit('show-message', '提交作业成功');
            } catch (error) {
                this.$emit('show-message', error.message || '提交作业失败');
            } finally {
                this.submitting = false;
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
        getStatusBadgeClass(homework) {
            if (homework.submission) {
                return 'bg-success';
            }
            return this.isDeadlinePassed(homework) ? 'bg-danger' : 'bg-warning';
        },
        getStatusText(homework) {
            if (homework.submission) {
                return '已提交';
            }
            return this.isDeadlinePassed(homework) ? '已截止' : '未提交';
        },
        isDeadlinePassed(homework) {
            return new Date(homework.deadline) < new Date();
        },
        formatDate(date) {
            return new Date(date).toLocaleString();
        }
    }
};
