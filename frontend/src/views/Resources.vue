<template>
  <div class="resources-container">
    <!-- 头部区域 -->
    <div class="header">
      <h2>资源管理</h2>
      <div class="header-controls">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索资源"
          style="width: 200px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 上传按钮 (仅管理员和教师可见) -->
        <el-button 
          v-if="['ADMIN', 'TEACHER','STUDENT'].includes(userRole)"
          type="primary" 
          @click="handleUpload"
        >
          <el-icon><Upload /></el-icon>
          上传资源
        </el-button>
        
        <!-- 排序按钮 -->
        <div class="sort-controls">
          <el-checkbox v-model="sortByDownloads" class="sort-checkbox">
            <span class="option-content">
              <el-icon class="sort-icon" :class="{'sort-desc': sortDirection === 'desc'}"><CaretBottom /></el-icon>
              按下载次数排序
            </span>
          </el-checkbox>
          <el-select v-model="sortDirection" class="sort-direction">
            <el-option label="降序" value="desc"></el-option>
            <el-option label="升序" value="asc"></el-option>
          </el-select>
        </div>
        
        <!-- 仅看我上传的资源开关 -->
        <el-switch
          v-model="onlyMyUploads"
          class="filter-item"
          active-text="仅看我的上传"
          inactive-text=""
          @change="handleFilterChange"
        />
      </div>
    </div>

    <!-- 资源列表 -->
    <el-table 
      v-loading="loading"
      :data="paginatedResources"
      style="width: 100%"
      :default-sort="{ prop: 'downloadCount', order: sortDirection }"
      @sort-change="handleTableSort"
      :row-class-name="tableRowClassName"
    >
      <el-table-column
        prop="name"
        label="资源名称"
        min-width="200"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <div class="resource-name">
            <el-icon><Document /></el-icon>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column
        prop="courseName"
        label="所属课程"
        min-width="150"
        show-overflow-tooltip
      />
      
      <el-table-column
        prop="description"
        label="描述"
        min-width="200"
        show-overflow-tooltip
      />
      
      <el-table-column
        prop="uploaderName"
        label="上传者"
        min-width="120"
        show-overflow-tooltip
      />
      
      <el-table-column
        prop="downloadCount"
        label="下载次数"
        min-width="100"
        sortable="custom"
      >
        <template #default="{ row }">
          <span class="download-count">
            <el-icon><Download /></el-icon>
            {{ row.downloadCount || 0 }}
          </span>
        </template>
      </el-table-column>
      
      <el-table-column
        prop="fileSize"
        label="文件大小"
        min-width="100"
      >
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      
      <el-table-column
        prop="createTime"
        label="上传时间"
        min-width="180"
        :formatter="(row) => formatDateTime(row.createTime)"
      />
      
      <el-table-column
        fixed="right"
        label="操作"
        width="150"
      >
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            @click="viewResourceDetail(row.id)"
          >
            查看
          </el-button>
          <el-button
            type="success"
            link
            @click="handleDownload(row)"
          >
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <el-button
            v-if="canDelete(row)"
            type="danger"
            link
            @click="handleDelete(row)"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 资源详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="资源详情"
      width="500px"
      :close-on-click-modal="false"
      center
    >
      <div v-if="currentResource" class="resource-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="资源名称">{{ currentResource.name }}</el-descriptions-item>
          <el-descriptions-item label="资源描述">{{ currentResource.description || '无描述' }}</el-descriptions-item>
          <el-descriptions-item label="课程名称">{{ currentResource.courseName }}</el-descriptions-item>
          <el-descriptions-item label="上传者">{{ currentResource.uploaderName }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(currentResource.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="下载次数">{{ currentResource.downloadCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ formatDateTime(currentResource.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="dialog-footer" style="margin-top: 20px; text-align: center;">
          <el-button type="primary" @click="handleDownload(currentResource)">下载资源</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 上传资源对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传资源"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="resourceFormRef"
        :model="resourceForm"
        :rules="resourceRules"
        label-width="80px"
      >
        <el-form-item label="资源名称" prop="name">
          <el-input 
            v-model="resourceForm.name" 
            placeholder="请输入资源名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="所属课程" prop="courseId">
          <el-select
            v-model="resourceForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="资源描述" prop="description">
          <el-input
            v-model="resourceForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入资源描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="资源文件" prop="file">
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            :auto-upload="false"
            :show-file-list="true"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="1"
            :file-list="fileList"
          >
            <template #trigger>
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择文件
              </el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                支持任意格式文件，单个文件大小不超过100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="uploading"
            :disabled="!fileList.length"
            @click="handleSubmit"
          >
            {{ uploading ? '上传中...' : '确定' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { 
  Search, 
  Upload, 
  Download, 
  Delete, 
  Document,
  CaretBottom
} from '@element-plus/icons-vue'
import service from '@/utils/request'

// Store
const store = useStore()
const userRole = computed(() => store.getters.user?.role || '')
const token = computed(() => store.getters.token)
const currentUserId = computed(() => store.getters.user?.id)

// 状态变量
const searchKeyword = ref('')
const resources = ref([])
const courses = ref([])
const uploadDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentResource = ref(null)
const resourceFormRef = ref(null)
const uploadRef = ref(null)
const fileList = ref([])
const loading = ref(false)
const sortByDownloads = ref(false)
const sortDirection = ref('desc')
const uploading = ref(false)
const onlyMyUploads = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单数据
const resourceForm = ref({
  name: '',
  courseId: '',
  description: '',
  file: null
})

// 表单验证规则
const resourceRules = {
  name: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入资源描述', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择要上传的文件', trigger: 'change' }
  ]
}

// 计算属性：过滤后的资源列表
const filteredResources = computed(() => {
  let result = resources.value

  // 仅看我上传的资源过滤
  if (onlyMyUploads.value && currentUserId.value) {
    result = result.filter(resource => resource.uploaderId === currentUserId.value)
  }

  // 课程过滤
  // if (selectedCourse.value) {
  //   result = result.filter(resource => resource.courseId === selectedCourse.value)
  // }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(resource => 
      resource.name.toLowerCase().includes(keyword) ||
      resource.description?.toLowerCase().includes(keyword) ||
      resource.courseName?.toLowerCase().includes(keyword) ||
      resource.originalFileName?.toLowerCase().includes(keyword) ||
      resource.uploaderName?.toLowerCase().includes(keyword)
    )
  }

  // 排序处理
  if (sortByDownloads.value) {
    result = [...result].sort((a, b) => {
      const compareResult = (a.downloadCount || 0) - (b.downloadCount || 0)
      return sortDirection.value === 'asc' ? compareResult : -compareResult
    })
  }

  return result
})

// 计算分页后的资源列表
const paginatedResources = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredResources.value.slice(start, end)
})

// 处理过滤条件变化
const handleFilterChange = () => {
  ElMessage.success(onlyMyUploads.value ? '已切换到仅看我的上传' : '已显示所有资源')
}

// 表格行类名
const tableRowClassName = ({ row, rowIndex }) => {
  const classes = []
  if (row.downloadCount > 0) {
    classes.push('table-row-highlight')
  }
  return classes.join(' ')
}

// 格式化日期时间
const formatDateTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

// 检查是否可以删除资源
const canDelete = (resource) => {
  const userRole = store.getters.user?.role
  const userId = store.getters.user?.id
  
  // 管理员可以删除任何资源
  if (userRole === 'ADMIN') {
    return true
  }
  
  // 教师和学生只能删除自己上传的资源
  if (['TEACHER', 'STUDENT'].includes(userRole)) {
    return resource.uploaderId === userId
  }
  
  return false
}

// 处理资源下载
const handleDownload = async (resource) => {
  try {
    loading.value = true
    await downloadResource(resource.id)
    await fetchResources()
    ElMessage({
      type: 'success',
      message: '下载成功',
    })
  } catch (error) {
    ElMessage({
      type: 'error',
      message: error.message || '下载失败',
    })
  } finally {
    loading.value = false
  }
}

// 处理资源删除
const handleDelete = (resource) => {
  ElMessageBox.confirm(
    `确定要删除资源"${resource.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      loading.value = true
      await deleteResource(resource.id)
      ElMessage({
        type: 'success',
        message: '删除成功',
      })
      await fetchResources()
    } catch (error) {
      ElMessage({
        type: 'error',
        message: error.message || '删除失败',
      })
    } finally {
      loading.value = false
    }
  }).catch(() => {})
}

// 处理资源上传
const handleUpload = () => {
  uploadDialogVisible.value = true
}

// 处理文件选择
const handleFileChange = (file) => {
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过100MB')
    uploadRef.value.clearFiles()
    return false
  }
  fileList.value = [file]
  resourceForm.value.file = file
}

// 处理文件移除
const handleFileRemove = () => {
  fileList.value = []
  resourceForm.value.file = null
}

// 处理对话框关闭
const handleDialogClose = () => {
  resourceForm.value = {
    name: '',
    courseId: '',
    description: '',
    file: null
  }
  fileList.value = []
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  if (resourceFormRef.value) {
    resourceFormRef.value.resetFields()
  }
}

// 获取资源列表
const fetchResources = async () => {
  try {
    loading.value = true
    const response = await service.get('/resource')
    console.log('Resources response:', response)
    if (response.data.code === 200) {
      resources.value = response.data.data.map(resource => ({
        ...resource,
        createTime: resource.createTime
      }))
    } else {
      ElMessage.error(response.data.message || '获取资源列表失败')
    }
  } catch (error) {
    console.error('获取资源列表失败:', error)
    ElMessage.error('获取资源列表失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 获取资源详情
const viewResourceDetail = async (id) => {
  try {
    loading.value = true
    const response = await service.get(`/resource/${id}`)
    if (response.data.code === 200) {
      currentResource.value = response.data.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取资源详情失败')
    }
  } catch (error) {
    console.error('获取资源详情失败:', error)
    ElMessage.error('获取资源详情失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    loading.value = true
    const response = await service.get('/courses/all')
    console.log('Courses response:', response)
    if (response.data.code === 200) {
      courses.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取课程列表失败')
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('请先登录')
      } else if (status === 403) {
        ElMessage.error('无权限访问课程列表')
      } else {
        ElMessage.error(error.response.data?.message || '获取课程列表失败')
      }
    } else {
      ElMessage.error('获取课程列表失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

// 上传资源
const handleSubmit = async () => {
  if (!resourceFormRef.value || !fileList.value.length) return
  
  try {
    await resourceFormRef.value.validate()
    
    const formData = new FormData()
    formData.append('name', resourceForm.value.name)
    formData.append('courseId', resourceForm.value.courseId)
    formData.append('description', resourceForm.value.description)
    formData.append('file', fileList.value[0].raw)
    
    uploading.value = true
    await service.post('/resource', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token.value}`
      }
    })
    
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    await fetchResources()
  } catch (error) {
    console.error('上传失败:', error)
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('请先登录')
      } else if (status === 403) {
        ElMessage.error('无权限执行此操作')
      } else {
        ElMessage.error(error.response.data?.message || '上传失败')
      }
    } else {
      ElMessage.error('上传失败，请检查网络连接')
    }
  } finally {
    uploading.value = false
  }
}

// 处理表格排序
const handleTableSort = (column) => {
  if (column.prop === 'downloadCount') {
    sortByDownloads.value = true
    sortDirection.value = column.order === 'ascending' ? 'asc' : 'desc'
  }
}

// 监听排序状态变化
watch([sortByDownloads, sortDirection], () => {
  // 当排序状态改变时，滚动到顶部
  const container = document.querySelector('.resources-container')
  if (container) {
    container.scrollTop = 0
  }
})

// 监听资源列表变化
watch(filteredResources, (newVal) => {
  total.value = newVal.length
})

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 处理每页数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 下载资源
const downloadResource = async (id) => {
  const loadingInstance = ElLoading.service({
    text: '正在下载...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  
  try {
    // 创建下载请求
    const response = await service({
      url: `/resource/${id}/download`,
      method: 'GET',
      responseType: 'blob',
      timeout: 30000, // 30秒超时
    });
    
    // 获取文件名
    const contentDisposition = response.headers['content-disposition'];
    let filename = 'download';
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (filenameMatch && filenameMatch[1]) {
        filename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''));
      }
    }
    
    // 创建Blob URL
    const blob = new Blob([response.data], { 
      type: response.headers['content-type'] || 'application/octet-stream' 
    });
    const url = window.URL.createObjectURL(blob);
    
    // 创建下载链接
    const link = document.createElement('a');
    link.style.display = 'none';
    link.href = url;
    link.download = filename;
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    
    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('下载成功');
  } catch (error) {
    console.error('下载失败:', error);
    let errorMessage = '下载失败';
    
    if (error.response) {
      switch (error.response.status) {
        case 404:
          errorMessage = '文件不存在';
          break;
        case 403:
          errorMessage = '没有下载权限';
          break;
        default:
          errorMessage = `下载失败: ${error.response.status}`;
      }
    } else if (error.code === 'ECONNABORTED') {
      errorMessage = '下载超时，请重试';
    }
    
    ElMessage.error(errorMessage);
  } finally {
    loadingInstance.close();
  }
}

// 删除资源
const deleteResource = async (id) => {
  try {
    await service.delete(`/resource/${id}`)
  } catch (error) {
    console.error('删除失败:', error)
    throw error
  }
}

// 初始化
fetchCourses()
fetchResources()
</script>

<style scoped>
.resources-container {
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  position: sticky;
  top: 0;
  background-color: #fff;
  z-index: 1;
  padding: 10px 0;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #f5f7fa;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.sort-controls:hover {
  background-color: #e4e7ed;
}

.sort-checkbox {
  margin: 0;
  display: flex;
  align-items: center;
}

.sort-direction {
  width: 90px;
}

.sort-icon {
  transition: transform 0.3s ease;
  margin-right: 4px;
  color: #409eff;
}

.sort-desc {
  transform: rotate(180deg);
}

.option-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.el-select-dropdown__item {
  padding: 0 12px;
}

.sort-checkbox :deep(.el-checkbox__label) {
  display: flex;
  align-items: center;
  color: #606266;
}

.sort-checkbox :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #409eff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  gap: 10px;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

:deep(.el-table) {
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.resource-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.resource-name .el-icon {
  color: #909399;
}

.download-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
}

.download-count .el-icon {
  font-size: 14px;
}

:deep(.el-table .cell) {
  white-space: nowrap;
}

:deep(.el-table__empty-block) {
  min-height: 200px;
}

:deep(.el-table__empty-text) {
  color: #909399;
}

:deep(.el-table .ascending .sort-caret.ascending) {
  border-bottom-color: #409eff;
}

:deep(.el-table .descending .sort-caret.descending) {
  border-top-color: #409eff;
}

.table-row-highlight {
  background-color: #f5f7fa;
}

.upload-demo {
  width: 100%;
}

.upload-demo :deep(.el-upload) {
  width: 100%;
}

.upload-demo :deep(.el-upload-dragger) {
  width: 100%;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.filter-item {
  margin-right: 15px;
}

.el-switch {
  margin-top: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
