// 格式化日期
export function formatDate(date) {
  if (!date) return ''
  
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hour}:${minute}`
}

// 格式化文件大小
export function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

// 格式化状态
export function formatStatus(status) {
  const statusMap = {
    pending: '待提交',
    submitted: '已提交',
    graded: '已评分',
    overdue: '已逾期'
  }
  return statusMap[status] || status
}

// 获取状态类型（用于el-tag的type属性）
export function getStatusType(status) {
  const typeMap = {
    pending: 'warning',
    submitted: 'success',
    graded: 'primary',
    overdue: 'danger'
  }
  return typeMap[status] || 'info'
}
