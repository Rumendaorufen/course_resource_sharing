<template>
  <div class="student-management">
    <h1>学生管理</h1>
    
    <!-- 课程选择区域 -->
    <div class="course-selection">
      <label for="course">选择课程：</label>
      <select id="course" v-model="selectedCourseId" @change="handleCourseChange">
      <option value="">请选择课程</option>
        <option v-for="course in courses" :key="course.id" :value="course.id">
          {{ course.name }}
        </option>
      </select>
    </div>
    
    <!-- 主功能区域 -->
    <div v-if="selectedCourseId" class="management-content">
      <div class="tabs">
        <div 
          :class="['tab', { active: activeTab === 'inCourse' }]" 
          @click="activeTab = 'inCourse'"
        >
          已选学生 ({{ studentsInCourse.length }})
        </div>
        <div 
          :class="['tab', { active: activeTab === 'addStudents' }]" 
          @click="activeTab = 'addStudents'"
        >
          添加学生
        </div>
      </div>
      
      <!-- 已选学生列表 -->
      <div v-show="activeTab === 'inCourse'" class="students-tab">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索学生姓名或学号"
            @input="handleSearchInCourse"
          />
        </div>
        
        <div class="students-table-container">
          <table class="students-table">
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>班级</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="student in filteredStudentsInCourse" :key="student.id">
                <td>{{ student.username }}</td>
                <td>{{ student.realName }}</td>
                <td>{{ student.classname || '-' }}</td>
                <td>
                  <button 
                    class="btn-remove" 
                    @click="removeStudent(student.id)"
                    :disabled="isLoading"
                  >
                    移除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="filteredStudentsInCourse.length === 0" class="empty-message">
            {{ isLoading ? '加载中...' : '暂无学生' }}
          </div>
        </div>
      </div>
      
      <!-- 添加学生页面 -->
      <div v-show="activeTab === 'addStudents'" class="add-students-tab">
        <div class="add-methods">
          <div class="method-card" @click="addMethod = 'byClass'">
            <h3>按班级批量添加</h3>
            <p>选择一个班级，将该班级所有学生添加到课程</p>
          </div>
          <div class="method-card" @click="addMethod = 'bySearch'">
            <h3>搜索单个添加</h3>
            <p>通过搜索找到特定学生并添加到课程</p>
          </div>
        </div>
        
        <!-- 按班级添加 -->
        <div v-if="addMethod === 'byClass'" class="add-by-class">
          <label for="classname">选择班级：</label>
          <select id="classname" v-model="selectedClassname" @change="fetchClassStudents">
            <option value="">请选择班级</option>
            <option v-for="className in classNames" :key="className" :value="className">
              {{ className }}
            </option>
          </select>
          <button 
            class="btn-add-class" 
            @click="addStudentsByClassMethod"
            :disabled="!selectedClassname || isLoading"
          >
            {{ isLoading ? '添加中...' : '添加班级学生' }}
          </button>
          
          <!-- 班级学生列表 -->
          <div v-if="selectedClassname" class="class-students-list">
            <h4>{{ selectedClassname }} 班级学生 ({{ classStudents.length }})</h4>
            <div class="students-table-container">
              <table class="students-table">
                <thead>
                  <tr>
                    <th>学号</th>
                    <th>姓名</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="student in classStudents" :key="student.id">
                    <td>{{ student.username }}</td>
                    <td>{{ student.realName }}</td>
                  </tr>
                </tbody>
              </table>
              <div v-if="isLoading" class="empty-message">加载中...</div>
              <div v-else-if="classStudents.length === 0" class="empty-message">暂无学生</div>
            </div>
          </div>
        </div>
        
        <!-- 搜索添加 -->
        <div v-if="addMethod === 'bySearch'" class="add-by-search">
          <div class="search-filters">
            <input 
              type="text" 
              v-model="notInCourseSearchKeyword" 
              placeholder="搜索学生姓名或学号"
              @input="handleSearchNotInCourse"
            />
            <select v-model="notInCourseClassname" @change="handleSearchNotInCourse">
              <option value="">全部班级</option>
              <option v-for="className in classNames" :key="className" :value="className">
                {{ className }}
              </option>
            </select>
          </div>
          
          <div class="students-table-container">
            <table class="students-table">
              <thead>
                <tr>
                  <th>学号</th>
                  <th>姓名</th>
                  <th>班级</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="student in studentsNotInCourse" :key="student.id">
                  <td>{{ student.username }}</td>
                <td>{{ student.realName }}</td>
                <td>{{ student.classname || '-' }}</td>
                  <td>
                    <button 
                      class="btn-add" 
                      @click="addSingleStudent(student.id)"
                      :disabled="isLoading"
                    >
                      添加
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="studentsNotInCourse.length === 0" class="empty-message">
              {{ isLoading ? '加载中...' : '暂无符合条件的学生' }}
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 未选择课程时的提示 -->
    <div v-else class="no-course-selected">
      <p>请先选择一个课程进行学生管理</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getCourses, getStudentsInCourse, getStudentsNotInCourse, 
         addStudentsByClass, addStudentToCourse, removeStudentFromCourse, 
         getAllClassNames } from '../api/course'

export default {
  name: 'StudentManagement',
  setup() {
    // 响应式数据
    const selectedCourseId = ref('')
    const courses = ref([])
    const studentsInCourse = ref([])
    const studentsNotInCourse = ref([])
    const classNames = ref([])
    const activeTab = ref('inCourse')
    const addMethod = ref('byClass')
    const selectedClassname = ref('')
    const notInCourseClassname = ref('')
    const searchKeyword = ref('')
    const notInCourseSearchKeyword = ref('')
    const isLoading = ref(false)
    const classStudents = ref([]) // 存储所选班级的学生列表
    
    // 计算属性：过滤后的已选学生列表
    const filteredStudentsInCourse = computed(() => {
      if (!searchKeyword.value) return studentsInCourse.value
      return studentsInCourse.value.filter(student => 
        student.username.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        student.realName.toLowerCase().includes(searchKeyword.value.toLowerCase())
      )
    })
    
    // 获取课程列表
    const fetchCourses = async () => {
      try {
        const response = await getCourses()
        console.log('API返回的课程数据:', response)
        console.log('response.data类型:', typeof response.data)
        console.log('response.data.data内容:', response.data.data)
        // 正确提取课程数组，后端返回结构为{code, message, data}，课程数组在data字段中
        const courseList = Array.isArray(response.data?.data) ? response.data.data : []
        courses.value = courseList
        console.log('courses数组填充后:', courses.value)
      } catch (error) {
        ElMessage.error('获取课程列表失败')
        console.error('Failed to fetch courses:', error)
      }
    }
    
    // 获取班级列表
    const fetchClassNames = async () => {
      try {
        const response = await getAllClassNames()
        console.log('API返回的班级数据:', response)
        // 正确提取班级数组，后端返回结构为{code, message, data}，班级数组在data字段中
        classNames.value = Array.isArray(response.data?.data) ? response.data.data : []
      } catch (error) {
        ElMessage.error('获取班级列表失败')
        console.error('Failed to fetch class names:', error)
      }
    }
    
    // 获取课程中的学生
    const fetchStudentsInCourse = async () => {
      if (!selectedCourseId.value) return
      
      isLoading.value = true
      try {
        const response = await getStudentsInCourse(selectedCourseId.value)
        // 正确提取学生数组，后端返回结构为{code, message, data}
        studentsInCourse.value = Array.isArray(response.data?.data) ? response.data.data : []
      } catch (error) {
        ElMessage.error('获取课程学生列表失败')
        console.error('Failed to fetch students in course:', error)
      } finally {
        isLoading.value = false
      }
    }
    
    // 获取不在课程中的学生
    const fetchStudentsNotInCourse = async () => {
      if (!selectedCourseId.value) return
      
      isLoading.value = true
      try {
        const response = await getStudentsNotInCourse(
          selectedCourseId.value,
          notInCourseSearchKeyword.value,
          notInCourseClassname.value
        )
        // 正确提取学生数组，后端返回结构为{code, message, data}
        studentsNotInCourse.value = Array.isArray(response.data?.data) ? response.data.data : []
      } catch (error) {
        ElMessage.error('获取学生列表失败')
        console.error('Failed to fetch students not in course:', error)
      } finally {
        isLoading.value = false
      }
    }
    
    // 处理课程选择变化
    const handleCourseChange = () => {
      activeTab.value = 'inCourse'
      fetchStudentsInCourse()
      if (addMethod.value === 'bySearch') {
        fetchStudentsNotInCourse()
      }
    }
    
    // 处理已选学生搜索
    const handleSearchInCourse = () => {
      // 计算属性会自动更新
    }
    
    // 处理未选学生搜索
    const handleSearchNotInCourse = () => {
      fetchStudentsNotInCourse()
    }
    
    // 获取班级学生列表
    const fetchClassStudents = async () => {
      console.log('开始获取班级学生列表')
      console.log('selectedClassname:', selectedClassname.value)
      console.log('selectedCourseId:', selectedCourseId.value)
      
      if (!selectedClassname.value || !selectedCourseId.value) {
        console.log('班级或课程ID为空，不获取学生列表')
        classStudents.value = []
        return
      }
      
      isLoading.value = true
      try {
        console.log('准备调用API获取班级学生')
        const response = await getStudentsNotInCourse(
          selectedCourseId.value,
          '', // 不使用关键词过滤
          selectedClassname.value
        )
        console.log('API调用成功，返回数据:', response)
        // 正确提取学生数组，后端返回结构为{code, message, data}
        const studentsData = Array.isArray(response.data?.data) ? response.data.data : []
        classStudents.value = studentsData
        console.log('班级学生列表更新成功，学生数量:', classStudents.value.length)
      } catch (error) {
        console.error('获取班级学生列表异常:', error)
        ElMessage.error('获取班级学生列表失败')
        classStudents.value = []
      } finally {
        isLoading.value = false
        console.log('获取班级学生列表操作完成')
      }
    }
    
    // 按班级添加学生
    const addStudentsByClassMethod = async () => {
      if (!selectedClassname.value) {
        ElMessage.warning('请选择班级')
        return
      }
      
      isLoading.value = true
      try {
        await addStudentsByClass(selectedCourseId.value, selectedClassname.value)
        ElMessage.success('班级学生添加成功')
        fetchStudentsInCourse()
        if (addMethod.value === 'bySearch') {
          fetchStudentsNotInCourse()
        }
        // 添加成功后刷新班级学生列表
        fetchClassStudents()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '添加失败')
        console.error('Failed to add students by class:', error)
      } finally {
        isLoading.value = false
      }
    }
    
    // 添加单个学生
    const addSingleStudent = async (studentId) => {
      isLoading.value = true
      try {
        await addStudentToCourse(selectedCourseId.value, studentId)
        ElMessage.success('学生添加成功')
        fetchStudentsInCourse()
        fetchStudentsNotInCourse()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '添加失败')
        console.error('Failed to add student:', error)
      } finally {
        isLoading.value = false
      }
    }
    
    // 移除学生
    const removeStudent = async (studentId) => {
      // 确认对话框
      if (!confirm('确定要将该学生从课程中移除吗？')) {
        return
      }
      
      isLoading.value = true
      try {
        await removeStudentFromCourse(selectedCourseId.value, studentId)
        ElMessage.success('学生移除成功')
        fetchStudentsInCourse()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '移除失败')
        console.error('Failed to remove student:', error)
      } finally {
        isLoading.value = false
      }
    }
    
    // 初始化
    onMounted(() => {
      fetchCourses()
      fetchClassNames()
    })
    
    return {
      selectedCourseId,
      courses,
      studentsInCourse,
      studentsNotInCourse,
      classNames,
      activeTab,
      addMethod,
      selectedClassname,
      notInCourseClassname,
      searchKeyword,
      notInCourseSearchKeyword,
      isLoading,
      classStudents,
      filteredStudentsInCourse,
      handleCourseChange,
      handleSearchInCourse,
      handleSearchNotInCourse,
      addStudentsByClassMethod,
      fetchClassStudents,
      addSingleStudent,
      removeStudent
    }
  }
}
</script>

<style scoped>
.student-management {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.student-management h1 {
  margin-bottom: 30px;
  color: #333;
}

.course-selection {
  margin-bottom: 30px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.course-selection label {
  display: inline-block;
  margin-right: 10px;
  font-weight: bold;
}

.course-selection select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-width: 300px;
}

.management-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.tabs {
  display: flex;
  background-color: #f0f2f5;
  border-bottom: 1px solid #dcdfe6;
}

.tab {
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}

.tab:hover {
  background-color: #e6f7ff;
}

.tab.active {
  background-color: white;
  border-bottom-color: #1890ff;
  color: #1890ff;
  font-weight: bold;
}

.students-tab,
.add-students-tab {
  padding: 20px;
}

.search-box {
  margin-bottom: 20px;
}

.search-box input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.students-table-container {
  overflow-x: auto;
}

.students-table {
  width: 100%;
  border-collapse: collapse;
}

.students-table th,
.students-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eaeaea;
}

.students-table th {
  background-color: #fafafa;
  font-weight: bold;
}

.btn-remove,
.btn-add {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-remove {
  background-color: #ff4d4f;
  color: white;
}

.btn-remove:hover {
  background-color: #ff7875;
}

.btn-add {
  background-color: #52c41a;
  color: white;
}

.btn-add:hover {
  background-color: #73d13d;
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.add-methods {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.method-card {
  padding: 20px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.method-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.method-card h3 {
  margin-bottom: 10px;
  color: #303133;
}

.method-card p {
  color: #606266;
}

.add-by-class,
.add-by-search {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.add-by-class label,
.add-by-class select,
.add-by-class button {
  margin-right: 10px;
}

.add-by-class select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.btn-add-class {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-add-class:hover:not(:disabled) {
  background-color: #409eff;
}

.btn-add-class:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.search-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-filters input,
.search-filters select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  flex: 1;
}

.no-course-selected {
  text-align: center;
  padding: 60px;
  color: #909399;
  font-size: 18px;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>