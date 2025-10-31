<template>
  <div class="courses-container">
    <!-- 头部区域 -->
    <div class="header">
      <h2>课程管理</h2>
      <div class="header-controls">
        <!-- 搜索框 -->
        <el-autocomplete
          v-model="searchKeyword"
          :fetch-suggestions="querySearch"
          placeholder="搜索课程"
          style="width: 200px"
          @select="handleSelect"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-autocomplete>
        
        <!-- 添加按钮 -->
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加课程
        </el-button>
      </div>
    </div>

    <!-- 课程表格 -->
    <el-table :data="paginatedCourses" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="课程ID" width="100" />
      <el-table-column prop="name" label="课程名称" />
      <el-table-column prop="description" label="课程描述" />
      <el-table-column prop="teacherName" label="教师" width="120" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
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
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredCourses.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingCourse ? '编辑课程' : '添加课程'"
      width="500px"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="courseForm.teacherId" style="width: 100%">
            <el-option
              v-for="item in teachers"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import service from '@/utils/request'

// 状态
const searchKeyword = ref('')
const courses = ref([])
const teachers = ref([])
const dialogVisible = ref(false)
const editingCourse = ref(null)
const courseFormRef = ref(null)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 表单数据
const courseForm = ref({
  name: '',
  description: '',
  teacherId: ''
})

// 表单验证规则
const courseRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }]
}

// 分页方法
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 计算属性：过滤后的课程列表
const filteredCourses = computed(() => {
  const keyword = searchKeyword.value.toLowerCase()
  return courses.value.filter(course => 
    course.name.toLowerCase().includes(keyword) ||
    course.description.toLowerCase().includes(keyword) ||
    course.teacherName.toLowerCase().includes(keyword)
  )
})

const paginatedCourses = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCourses.value.slice(start, end)
})

// 获取课程列表
const fetchCourses = async () => {
  try {
    const { data } = await service.get('/courses')
    if (data.code === 200) {
      courses.value = data.data
    } else {
      ElMessage.error(data.message || '获取课程列表失败')
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const { data } = await service.get('/users/teachers')
    if (data.code === 200) {
      teachers.value = data.data
    } else {
      ElMessage.error(data.message || '获取教师列表失败')
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
  }
}

// 搜索建议
const querySearch = (queryString, cb) => {
  const results = queryString
    ? courses.value.filter(course => 
        course.name.toLowerCase().includes(queryString.toLowerCase())
      )
    : courses.value

  cb(results.map(course => ({
    value: course.name,
    course
  })))
}

// 选择搜索结果
const handleSelect = (item) => {
  searchKeyword.value = item.value
}

// 添加课程
const handleAdd = () => {
  editingCourse.value = null
  courseForm.value = {
    name: '',
    description: '',
    teacherId: ''
  }
  dialogVisible.value = true
}

// 编辑课程
const handleEdit = (course) => {
  editingCourse.value = course
  courseForm.value = { ...course }
  dialogVisible.value = true
}

// 删除课程
const handleDelete = async (course) => {
  try {
    await ElMessageBox.confirm('确定要删除这门课程吗？', '提示', {
      type: 'warning'
    })
    const { data } = await service.delete(`/courses/${course.id}`)
    if (data.code === 200) {
      ElMessage.success('删除成功')
      fetchCourses()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除课程失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!courseFormRef.value) return
  
  try {
    await courseFormRef.value.validate()
    const url = editingCourse.value
      ? `/courses/${editingCourse.value.id}`
      : '/courses'
    const method = editingCourse.value ? 'put' : 'post'
    
    const { data } = await service[method](url, courseForm.value)
    
    if (data.code === 200) {
      ElMessage.success(editingCourse.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchCourses()
    } else {
      ElMessage.error(data.message || (editingCourse.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交课程表单失败:', error)
    ElMessage.error(editingCourse.value ? '更新失败' : '添加失败')
  }
}

// 初始化
fetchTeachers()
fetchCourses()
</script>

<style scoped>
.courses-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-controls {
  display: flex;
  gap: 16px;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
