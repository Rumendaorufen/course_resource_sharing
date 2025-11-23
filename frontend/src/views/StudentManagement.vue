<template>
  <div class="student-management">
    <h1>学生管理</h1>
    
    <!-- 课程选择区域 - 卡片式展示 -->
    <div class="course-cards-container">
      <div 
        v-for="course in courses" 
        :key="course.id"
        :class="['course-card', { 'active': selectedCourseId === course.id }]"
        @click="selectCourse(course.id)"
      >
        <div class="course-image">
          <span class="course-name-display">{{ course.name }}</span>
        </div>
        <div class="course-info">
          <p class="course-teacher">{{ course.teacherName || '未知教师' }}</p>
        </div>
      </div>
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
import { useStore } from 'vuex'
import { getCourses, getTeacherCourses, getStudentsInCourse, getStudentsNotInCourse, 
         addStudentsByClass, addStudentToCourse, removeStudentFromCourse, 
         getAllClassNames } from '../api/course'

export default {
  name: 'StudentManagement',
  setup() {
    const store = useStore()
    const currentUser = computed(() => store.getters.user)
    const userRole = computed(() => currentUser.value?.role)
    
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
        let response;
        
        // 根据用户角色调用不同的接口
        if (userRole.value === 'TEACHER' && currentUser.value?.id) {
          // 教师角色调用获取教师课程的接口
          console.log('教师角色，调用教师专用课程接口')
          response = await getTeacherCourses(currentUser.value.id)
        } else {
          // 其他角色调用获取全部课程的接口
          console.log('非教师角色，调用全部课程接口')
          response = await getCourses()
        }
        
        console.log('API返回的课程数据:', response)
        // 正确提取课程数组，后端返回结构为{code, message, data}，课程数组在data字段中
        const courseList = Array.isArray(response.data?.data) ? response.data.data : []
        
        // 为课程添加teacherName字段（如果不存在）
        courses.value = courseList.map(course => ({
          ...course,
          teacherName: course.teacherName || course.teacher || currentUser.value?.realName || '未知教师'
        }))
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
    
    // 处理课程选择
    const selectCourse = (courseId) => {
      selectedCourseId.value = courseId
      activeTab.value = 'inCourse'
      fetchStudentsInCourse()
      if (addMethod.value === 'bySearch') {
        fetchStudentsNotInCourse()
      }
    }
    
    // 处理课程选择变化（保留以兼容其他调用）
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
      selectCourse,
      handleSearchInCourse,
      handleSearchNotInCourse,
      addStudentsByClassMethod,
      fetchClassStudents,
      addSingleStudent,
      removeStudent,
      currentUser,
      userRole
    }
  }
}
</script>

<style scoped>
@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
.student-management {
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #f5f7fa 100%);
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #303133;
  line-height: 1.6;
}

.student-management h1 {
  margin-bottom: 30px;
  color: #1f2937;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.course-cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.course-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  position: relative;
  height: 160px;
  overflow: hidden;
  background-image: radial-gradient(circle at 10% 20%, rgba(64, 158, 255, 0.03) 0%, transparent 20%),
                    radial-gradient(circle at 80% 70%, rgba(99, 102, 241, 0.03) 0%, transparent 25%);
}

.course-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  border-color: rgba(64, 158, 255, 0.3);
}

.course-card::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #409eff, #667eea, #764ba2, #409eff);
  border-radius: 18px;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s;
}

.course-card:hover::before {
  opacity: 1;
  animation: gradient 3s ease infinite;
  background-size: 400% 400%;
}

.course-card.active {
  border-color: #409eff;
  box-shadow: 0 10px 28px rgba(64, 158, 255, 0.3);
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
  background-image: radial-gradient(circle at 10% 20%, rgba(64, 158, 255, 0.1) 0%, transparent 30%),
                    radial-gradient(circle at 80% 70%, rgba(99, 102, 241, 0.08) 0%, transparent 35%);
}

.course-image {
  height: 100px;
  background: linear-gradient(135deg, #ffffff 0%, #f3f4f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.7);
  position: relative;
  overflow: hidden;
}

.course-image::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(64, 158, 255, 0.08) 0%, transparent 30%),
    radial-gradient(circle at 75% 75%, rgba(99, 102, 241, 0.08) 0%, transparent 30%);
  pointer-events: none;
}

.course-image::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 20% 20%, rgba(64, 158, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.course-name-display {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  letter-spacing: -0.3px;
  position: relative;
  z-index: 1;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.course-card:hover .course-name-display {
  transform: scale(1.03);
  letter-spacing: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.course-info {
  padding: 12px;
  text-align: center;
}

.course-teacher {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}


.management-content {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
}

.tabs {
  display: flex;
  background-color: rgba(240, 242, 245, 0.7);
  border-bottom: 1px solid #e5e7eb;
}

.tab {
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}

.tab:hover {
  background-color: #e0e0e0;
}

.tab.active {
  background-color: white;
  border-bottom-color: #409eff;
  color: #409eff;
  font-weight: 600;
  font-size: 15px;
}

.students-tab,
.add-students-tab {
  padding: 25px;
  background-color: white;
}

.search-box {
  margin-bottom: 20px;
}

.search-box input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: white;
  transition: all 0.3s;
}

.search-box input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
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
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
}

.students-table th {
  background-color: #f9fafb;
  font-weight: 600;
  color: #374151;
  letter-spacing: -0.1px;
}

.students-table td {
  color: #4b5563;
  font-weight: 450;
}

.btn-remove,
.btn-add {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  font-size: 14px;
}

.btn-remove {
  background-color: #f43f5e;
  color: white;
}

.btn-remove:hover {
  background-color: #ef4444;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.btn-add {
  background-color: #3b82f6;
  color: white;
}

.btn-add:hover {
  background-color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 16px;
  font-weight: 500;
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
  border-color: #666666;
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
  background-color: rgba(249, 250, 251, 0.95);
  border-radius: 10px;
  border: 1px solid #e5e7eb;
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
  padding: 10px 20px;
  background-color: white;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.btn-add-class:hover:not(:disabled) {
  color: #409eff;
  border-color: #409eff;
  background-color: #ecf5ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
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
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  flex: 1;
  background-color: white;
  transition: all 0.3s;
}

.search-filters input:focus,
.search-filters select:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.no-course-selected {
  text-align: center;
  padding: 60px;
  color: #6b7280;
  font-size: 18px;
  font-weight: 500;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>