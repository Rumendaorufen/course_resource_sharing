const ResourcesComponent = {
    template: `
        <div class="resources">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>课程资源</h2>
                <div>
                    <select class="form-select d-inline-block w-auto me-2" v-model="selectedCourseId" @change="loadResources">
                        <option value="">选择课程</option>
                        <option v-for="course in courses" :key="course.id" :value="course.id">
                            {{ course.name }}
                        </option>
                    </select>
                    <button class="btn btn-primary" @click="showUploadModal" :disabled="!selectedCourseId">
                        <i class="mdi mdi-upload"></i> 上传资源
                    </button>
                </div>
            </div>

            <!-- 资源列表 -->
            <div class="card" v-if="selectedCourseId">
                <div class="card-body">
                    <div v-if="resources.length === 0" class="text-center py-4">
                        <p class="text-muted">暂无资源</p>
                    </div>
                    <div class="table-responsive">
                        <table class="table" v-else>
                            <thead>
                                <tr>
                                    <th>文件名</th>
                                    <th>上传时间</th>
                                    <th>大小</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="resource in resources" :key="resource.id">
                                    <td>{{ resource.originalFileName }}</td>
                                    <td>{{ formatDate(resource.createTime) }}</td>
                                    <td>{{ formatFileSize(resource.fileSize) }}</td>
                                    <td>
                                        <button class="btn btn-outline-primary btn-sm me-2"
                                                @click="downloadResource(resource)">
                                            <i class="mdi mdi-download"></i> 下载
                                        </button>
                                        <button class="btn btn-outline-danger btn-sm"
                                                @click="deleteResource(resource.id)"
                                                v-if="userRole === 'TEACHER'">
                                            <i class="mdi mdi-delete"></i> 删除
                                        </button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- 上传资源模态框 -->
            <div class="modal fade" ref="uploadModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">上传资源</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <form @submit.prevent="uploadResource">
                                <div class="mb-3">
                                    <label class="form-label">选择文件</label>
                                    <input type="file" class="form-control" ref="fileInput" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">描述</label>
                                    <textarea class="form-control" v-model="resourceForm.description" rows="3"></textarea>
                                </div>
                                <div class="text-end">
                                    <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">取消</button>
                                    <button type="submit" class="btn btn-primary" :disabled="uploading">
                                        <span class="spinner-border spinner-border-sm" v-if="uploading"></span>
                                        {{ uploading ? '上传中...' : '上传' }}
                                    </button>
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
            resources: [],
            selectedCourseId: '',
            uploading: false,
            resourceForm: {
                description: ''
            },
            modal: null
        }
    },
    mounted() {
        this.loadCourses();
        this.modal = new bootstrap.Modal(this.$refs.uploadModal);
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
        async loadResources() {
            if (!this.selectedCourseId) {
                this.resources = [];
                return;
            }
            try {
                const response = await apiService.resource.list(this.selectedCourseId);
                this.resources = response.data;
            } catch (error) {
                this.$emit('show-message', error.message || '加载资源失败');
            }
        },
        showUploadModal() {
            this.resourceForm.description = '';
            this.modal.show();
        },
        async uploadResource() {
            const file = this.$refs.fileInput.files[0];
            if (!file) return;

            const formData = new FormData();
            formData.append('file', file);
            formData.append('description', this.resourceForm.description);

            this.uploading = true;
            try {
                await apiService.resource.upload(this.selectedCourseId, formData);
                this.modal.hide();
                this.loadResources();
                this.$emit('show-message', '上传资源成功');
            } catch (error) {
                this.$emit('show-message', error.message || '上传资源失败');
            } finally {
                this.uploading = false;
            }
        },
        async downloadResource(resource) {
            try {
                const response = await apiService.resource.download(resource.id);
                const url = window.URL.createObjectURL(new Blob([response]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', resource.originalFileName);
                document.body.appendChild(link);
                link.click();
                link.remove();
            } catch (error) {
                this.$emit('show-message', error.message || '下载资源失败');
            }
        },
        async deleteResource(id) {
            if (!confirm('确定要删除这个资源吗？')) return;
            
            try {
                await apiService.resource.delete(id);
                this.loadResources();
                this.$emit('show-message', '删除资源成功');
            } catch (error) {
                this.$emit('show-message', error.message || '删除资源失败');
            }
        },
        formatDate(date) {
            return new Date(date).toLocaleString();
        },
        formatFileSize(bytes) {
            if (bytes === 0) return '0 B';
            const k = 1024;
            const sizes = ['B', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
        }
    },
    watch: {
        selectedCourseId() {
            this.loadResources();
        }
    }
};
