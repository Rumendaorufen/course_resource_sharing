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
    
    <!-- 数据统计卡片 -->
    <div class="stats-cards">
      <DataCard
        title="总课程数"
        :value="coursesCount"
        icon="Document"
        :trend="coursesTrend"
        trendType="increase"
      />
      <DataCard
        title="教师人数"
        :value="teachersCount"
        icon="User"
        color="primary"
        :trend="teacherTrend"
        trendType="increase"
      />
      <DataCard
        title="筛选后课程"
        :value="filteredCourses.length"
        icon="Filter"
        color="warning"
      />
      <DataCard
        title="课程类别数"
        :value="categoryCount"
        icon="Fold"
        color="info"
      />
    </div>
    
    <!-- 数据可视化区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>课程名称分布</span>
            </div>
          </template>
          <div ref="courseNameChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>教师课程分布</span>
            </div>
          </template>
          <div ref="teacherChart" class="chart-container"></div>
        </el-card>
      </el-col>

    </el-row>

    <!-- 课程表格 -->
    <AdvancedTable
      :data="filteredCourses"
      :columns="columns"
      :loading="false"
      :show-selection="true"
      :show-pagination="true"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="filteredCourses.length"
      :show-action="true"
      :actions="tableActions"
      :show-search="true"
      :search-query="searchKeyword"
      :search-placeholder="'搜索课程名称、描述或教师'"
      :filter-func="filterCourse"
      @selection-change="handleSelectionChange"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      @action="handleTableAction"
    >
        <!-- 操作按钮通过tableActions配置 -->
    </AdvancedTable>

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

<style scoped>
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
</style>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Document, User, Filter, Fold } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import service from '@/utils/request'

import DataCard from '@/components/DataCard.vue'
import AdvancedTable from '@/components/AdvancedTable.vue'

// 状态
const searchKeyword = ref('')
const courses = ref([])
const teachers = ref([])
const dialogVisible = ref(false)
const editingCourse = ref(null)
const courseFormRef = ref(null)

// 图表引用
const courseNameChart = ref(null)
const teacherChart = ref(null)
const courseNameChartInstance = ref(null)
const teacherChartInstance = ref(null)

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

// 表格列配置
const columns = ref([
  {
    prop: 'id',
    label: '课程ID',
    width: 100
  },
  {
    prop: 'name',
    label: '课程名称',
    minWidth: 200,
    sortable: true
  },
  {
    prop: 'description',
    label: '课程描述',
    minWidth: 250
  },
  {
    prop: 'teacherName',
    label: '教师',
    width: 120,
    sortable: true
  }
])

// 表格操作按钮配置
const tableActions = ref([
  {
    key: 'edit',
    text: '编辑',
    type: 'primary',
    size: 'small',
    icon: Edit
  },
  {
    key: 'delete',
    text: '删除',
    type: 'danger',
    size: 'small',
    icon: Delete
  }
])

// 统计卡片趋势数据
const coursesTrend = ref(12.5)
const teacherTrend = ref(8.3)

// 选择的行数据
const selectedRows = ref([])

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

// 统计数据计算属性
const coursesCount = computed(() => courses.value.length)
const teachersCount = computed(() => teachers.value.length)
const categoryCount = computed(() => {
  // 提取所有可能的类别（这里使用教师作为类别示例）
  const categories = new Set(courses.value.map(course => course.teacherName))
  return categories.size
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

// 课程过滤方法
function filterCourse(item, query) {
  if (!query) return true
  
  const lowerQuery = query.toLowerCase()
  return (
    item.name.toLowerCase().includes(lowerQuery) ||
    item.description.toLowerCase().includes(lowerQuery) ||
    item.teacherName.toLowerCase().includes(lowerQuery)
  )
}

// 处理表格操作按钮点击
function handleTableAction(action, row) {
  switch (action.key) {
    case 'edit':
      handleEdit(row)
      break
    case 'delete':
      handleDelete(row)
      break
    default:
      break
  }
}

// 处理表格选择变化
function handleSelectionChange(rows) {
  selectedRows.value = rows
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

// 初始化课程名称分布图表（柱状图）
const initCourseNameChart = () => {
  if (!courseNameChart.value) return
  
  // 销毁已有实例
  if (courseNameChartInstance.value) {
    courseNameChartInstance.value.dispose()
  }
  
  courseNameChartInstance.value = echarts.init(courseNameChart.value)
  
  // 按课程名称统计课程数量（处理重名课程）
  const courseNameStats = {}
  courses.value.forEach(course => {
    courseNameStats[course.name] = (courseNameStats[course.name] || 0) + 1
  })
  
  // 排序并限制数量
  const sortedCourses = Object.entries(courseNameStats)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedCourses.map(item => item[0]),
      axisLabel: {
        interval: 0,
        rotate: 45,
        formatter: function(value) {
          // 如果名称太长，可以截断显示
          return value.length > 8 ? value.substring(0, 8) + '...' : value;
        }
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '课程数量',
        type: 'bar',
        data: sortedCourses.map(item => item[1]),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  }
  
  courseNameChartInstance.value.setOption(option)
}

// 初始化教师课程分布图表（饼图）
const initTeacherChart = () => {
  if (!teacherChart.value) return
  
  // 销毁已有实例
  if (teacherChartInstance.value) {
    teacherChartInstance.value.dispose()
  }
  
  teacherChartInstance.value = echarts.init(teacherChart.value)
  
  // 统计每个教师的课程数量
  const teacherStats = {}
  courses.value.forEach(course => {
    const teacherName = course.teacherName || '未知教师'
    teacherStats[teacherName] = (teacherStats[teacherName] || 0) + 1
  })
  
  const data = Object.entries(teacherStats).map(([key, value]) => ({
    name: key,
    value
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '教师课程',
        type: 'pie',
        radius: '65%',
        center: ['60%', '50%'],
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    ],
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#C0C4CC', '#B1B3B8', '#DCDFE6', '#ECF5FF', '#F0F9EB']
  }
  
  teacherChartInstance.value.setOption(option)
}



// 更新图表
const updateCharts = () => {
  // 延迟执行以确保DOM已更新
  setTimeout(() => {
    initCourseNameChart()
    initTeacherChart()
  }, 100)
}

// 监听课程数据变化，更新图表
watch(() => courses.value.length, () => {
  updateCharts()
})

// 监听窗口大小变化
const handleResize = () => {
  if (courseNameChartInstance.value) {
    courseNameChartInstance.value.resize()
  }
  if (teacherChartInstance.value) {
    teacherChartInstance.value.resize()
  }
}

// 初始化
fetchTeachers()
fetchCourses()

// 生命周期钩子
onMounted(async () => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (courseNameChartInstance.value) {
    courseNameChartInstance.value.dispose()
  }
  if (teacherChartInstance.value) {
    teacherChartInstance.value.dispose()
  }
})
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

h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
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

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 20px;
}

.charts-section {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 15px;
}

.card-icon.primary {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.card-icon.success {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.card-icon.warning {
  background-color: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.card-icon.info {
  background-color: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.card-text {
  flex: 1;
}

.card-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.card-label {
  color: #909399;
  font-size: 14px;
}

/* 图表容器样式 */
.chart-container {
  width: 100%;
  height: 350px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
  
  .card-number {
    font-size: 24px;
  }
  
  .card-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-controls {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
