import request from '../utils/request'

// 获取所有课程
export const getCourses = () => {
  return request.get('/courses/all')
}

// 获取课程中的学生列表
export const getStudentsInCourse = (courseId) => {
  return request.get(`/courses/${courseId}/students`)
}

// 获取不在课程中的学生列表
export const getStudentsNotInCourse = (courseId, keyword, classname) => {
  return request.get(`/courses/${courseId}/students/not-enrolled`, {
    params: {
      keyword,
      classname
    }
  })
}

// 按班级批量添加学生到课程
export const addStudentsByClass = (courseId, classname) => {
  return request.post(`/courses/${courseId}/students/class`, {
    classname
  })
}

// 添加单个学生到课程
export const addStudentToCourse = (courseId, studentId) => {
  return request.post(`/courses/${courseId}/students/${studentId}`)
}

// 从课程中移除学生
export const removeStudentFromCourse = (courseId, studentId) => {
  return request.delete(`/courses/${courseId}/students/${studentId}`)
}

// 获取所有班级名称列表
export const getAllClassNames = () => {
  return request.get('/courses/classnames')
}