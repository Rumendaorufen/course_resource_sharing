<template>
  <div class="advanced-table">
    <!-- 工具栏 -->
    <div v-if="showToolbar" class="table-toolbar">
      <!-- 自定义工具栏左侧内容 -->
      <div class="toolbar-left" v-if="$slots.toolbarLeft">
        <slot name="toolbarLeft"></slot>
      </div>
      
      <!-- 搜索框 -->
      <div v-if="showSearch" class="toolbar-search">
        <el-input
          v-model="searchQuery"
          :placeholder="searchPlaceholder"
          clearable
          :prefix-icon="Search"
          @input="handleSearch"
          :debounce="searchDebounce"
        />
      </div>
      
      <!-- 批量操作按钮 -->
      <div v-if="showBatchActions && selectedRows.length > 0" class="toolbar-batch-actions">
        <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button-group>
          <el-button 
            v-for="action in batchActions" 
            :key="action.key"
            :type="action.type || 'default'"
            :size="action.size || 'small'"
            @click="handleBatchAction(action)"
          >
            <template v-if="action.icon">
              <el-icon><component :is="action.icon" /></el-icon>
            </template>
            {{ action.text }}
          </el-button>
          <el-button 
            type="default" 
            size="small"
            @click="clearSelection"
          >
            取消选择
          </el-button>
        </el-button-group>
      </div>
      
      <!-- 自定义工具栏右侧内容 -->
      <div class="toolbar-right" v-if="$slots.toolbarRight">
        <slot name="toolbarRight"></slot>
      </div>
    </div>
    
    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      :height="height"
      :stripe="stripe"
      :border="border"
      :fit="fit"
      :highlight-current-row="highlightCurrentRow"
      :row-class-name="rowClassName"
      :row-style="rowStyle"
      :cell-class-name="cellClassName"
      :cell-style="cellStyle"
      :header-row-class-name="headerRowClassName"
      :header-row-style="headerRowStyle"
      :header-cell-class-name="headerCellClassName"
      :header-cell-style="headerCellStyle"
      :current-row-key="currentRowKey"
      :row-key="rowKey"
      :default-expand-all="defaultExpandAll"
      :expand-row-keys="expandRowKeys"
      :max-height="maxHeight"
      :empty-text="emptyText"
      :pagination-layout="paginationLayout"
      @current-change="handleCurrentChange"
      @select="handleSelect"
      @select-all="handleSelectAll"
      @selection-change="handleSelectionChange"
      @cell-mouse-enter="handleCellMouseEnter"
      @cell-mouse-leave="handleCellMouseLeave"
      @cell-click="handleCellClick"
      @cell-dblclick="handleCellDblclick"
      @row-click="handleRowClick"
      @row-dblclick="handleRowDblclick"
      @header-click="handleHeaderClick"
      @header-contextmenu="handleHeaderContextmenu"
      @row-contextmenu="handleRowContextmenu"
      @sort-change="handleSortChange"
      @filter-change="handleFilterChange"
      @expand-change="handleExpandChange"
      @select-cancel="handleSelectCancel"
      @select-all-cancel="handleSelectAllCancel"
      ref="tableRef"
    >
      <!-- 复选框列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        :width="selectionWidth"
        :selectable="selectable"
        :reserve-selection="reserveSelection"
        fixed="left"
      />
      
      <!-- 展开列 -->
      <template v-if="$slots.expand">
        <el-table-column type="expand" fixed="left">
          <template #default="scope">
            <slot name="expand" v-bind="scope"></slot>
          </template>
        </el-table-column>
      </template>
      
      <!-- 自定义索引列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        :label="indexLabel"
        :width="indexWidth"
        :fixed="indexFixed"
        :index="indexMethod"
      />
      
      <!-- 动态列 -->
      <template v-for="column in columns" :key="column.prop || column.key">
        <!-- 多级表头 -->
        <el-table-column
          v-if="column.children && column.children.length > 0"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align"
          :header-align="column.headerAlign"
          :sortable="column.sortable"
          :resizable="column.resizable"
          :show-overflow-tooltip="column.showOverflowTooltip"
          :fixed="column.fixed"
          :type="column.type"
        >
          <template v-for="childColumn in column.children" :key="childColumn.prop || childColumn.key">
            <el-table-column
              v-if="!childColumn.children || childColumn.children.length === 0"
              v-bind="childColumn"
            >
              <template v-if="childColumn.slot" #default="scope">
                <slot :name="childColumn.slot" v-bind="scope"></slot>
              </template>
            </el-table-column>
          </template>
        </el-table-column>
        
        <!-- 普通列 -->
        <el-table-column
          v-else
          v-bind="column"
        >
          <template v-if="column.slot" #default="scope">
            <slot :name="column.slot" v-bind="scope"></slot>
          </template>
        </el-table-column>
      </template>
      
      <!-- 操作列 -->
      <el-table-column
        v-if="showAction"
        :label="actionLabel"
        :width="actionWidth"
        :fixed="actionFixed"
        :align="actionAlign"
      >
        <template #default="scope">
          <!-- 自定义操作按钮 -->
          <slot name="action" v-bind="scope"></slot>
          
          <!-- 默认操作按钮 -->
          <template v-if="actions && actions.length > 0">
            <el-button
              v-for="action in actions"
              :key="action.key"
              :type="action.type || 'text'"
              :size="action.size || 'small'"
              :disabled="action.disabled && action.disabled(scope)"
              :icon="action.icon"
              @click="handleAction(action, scope)"
              :show-overflow-tooltip="true"
            >
              {{ action.text }}
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div v-if="showPagination" class="table-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="localPageSize"
        :page-sizes="pageSizes"
        :page-count="totalPages"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 加载更多 -->
    <div v-if="showLoadMore && !showPagination" class="load-more">
      <el-button
        :loading="loading"
        :disabled="loading || !hasMore"
        type="text"
        @click="loadMore"
      >
        {{ loading ? '加载中...' : (hasMore ? '加载更多' : '没有更多数据了') }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  
  // 表格列配置
  columns: {
    type: Array,
    default: () => []
  },
  
  // 是否显示加载状态
  loading: {
    type: Boolean,
    default: false
  },
  
  // 表格高度
  height: {
    type: [String, Number],
    default: ''
  },
  
  // 是否显示斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  
  // 是否显示边框
  border: {
    type: Boolean,
    default: false
  },
  
  // 列的宽度是否自撑开
  fit: {
    type: Boolean,
    default: true
  },
  
  // 是否高亮当前行
  highlightCurrentRow: {
    type: Boolean,
    default: false
  },
  
  // 行的 className 的回调方法
  rowClassName: {
    type: [String, Function],
    default: ''
  },
  
  // 行的 style 的回调方法
  rowStyle: {
    type: [Object, Function],
    default: () => ({})
  },
  
  // 单元格的 className 的回调方法
  cellClassName: {
    type: [String, Function],
    default: ''
  },
  
  // 单元格的 style 的回调方法
  cellStyle: {
    type: [Object, Function],
    default: () => ({})
  },
  
  // 表头行的 className
  headerRowClassName: {
    type: [String, Function],
    default: ''
  },
  
  // 表头行的 style
  headerRowStyle: {
    type: [Object, Function],
    default: () => ({})
  },
  
  // 表头单元格的 className
  headerCellClassName: {
    type: [String, Function],
    default: ''
  },
  
  // 表头单元格的 style
  headerCellStyle: {
    type: [Object, Function],
    default: () => ({})
  },
  
  // 当前行的 key
  currentRowKey: {
    type: [String, Number],
    default: ''
  },
  
  // 行数据的 Key
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  
  // 是否默认展开所有行
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  
  // 可以展开的行，控制属性
  expandRowKeys: {
    type: Array,
    default: () => []
  },
  
  // 最大高度
  maxHeight: {
    type: [String, Number],
    default: ''
  },
  
  // 空数据时显示的文本
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  
  // 是否显示工具栏
  showToolbar: {
    type: Boolean,
    default: true
  },
  
  // 是否显示搜索框
  showSearch: {
    type: Boolean,
    default: true
  },
  
  // 搜索占位符
  searchPlaceholder: {
    type: String,
    default: '搜索'
  },
  
  // 搜索防抖时间
  searchDebounce: {
    type: Number,
    default: 500
  },
  
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  
  // 当前页码（支持v-model）
  modelValue: {
    type: Number,
    default: 1
  },
  
  // 每页条数（支持v-model）
  pageSize: {
    type: Number,
    default: 10
  },
  
  // 每页条数选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  
  // 分页布局
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  
  // 是否显示复选框列
  showSelection: {
    type: Boolean,
    default: false
  },
  
  // 复选框列宽度
  selectionWidth: {
    type: [String, Number],
    default: 50
  },
  
  // 是否可选择的方法
  selectable: {
    type: Function,
    default: () => true
  },
  
  // 是否在数据更新之后保留之前选中的数据
  reserveSelection: {
    type: Boolean,
    default: false
  },
  
  // 是否显示索引列
  showIndex: {
    type: Boolean,
    default: false
  },
  
  // 索引列标签
  indexLabel: {
    type: String,
    default: '序号'
  },
  
  // 索引列宽度
  indexWidth: {
    type: [String, Number],
    default: 60
  },
  
  // 索引列是否固定
  indexFixed: {
    type: Boolean,
    default: false
  },
  
  // 索引方法
  indexMethod: {
    type: Function,
    default: (index) => index + 1
  },
  
  // 是否显示操作列
  showAction: {
    type: Boolean,
    default: false
  },
  
  // 操作列标签
  actionLabel: {
    type: String,
    default: '操作'
  },
  
  // 操作列宽度
  actionWidth: {
    type: [String, Number],
    default: 180
  },
  
  // 操作列是否固定
  actionFixed: {
    type: String,
    default: 'right'
  },
  
  // 操作列对齐方式
  actionAlign: {
    type: String,
    default: 'center'
  },
  
  // 操作按钮配置
  actions: {
    type: Array,
    default: () => []
  },
  
  // 是否显示批量操作
  showBatchActions: {
    type: Boolean,
    default: false
  },
  
  // 批量操作按钮配置
  batchActions: {
    type: Array,
    default: () => []
  },
  
  // 是否显示加载更多
  showLoadMore: {
    type: Boolean,
    default: false
  },
  
  // 是否有更多数据
  hasMore: {
    type: Boolean,
    default: true
  },
  
  // 搜索过滤函数
  filterFunc: {
    type: Function,
    default: null
  }
})

// Emits
const emit = defineEmits([
  'update:modelValue',
  'update:pageSize',
  'current-change',
  'select',
  'select-all',
  'selection-change',
  'cell-mouse-enter',
  'cell-mouse-leave',
  'cell-click',
  'cell-dblclick',
  'row-click',
  'row-dblclick',
  'header-click',
  'header-contextmenu',
  'row-contextmenu',
  'sort-change',
  'filter-change',
  'expand-change',
  'select-cancel',
  'select-all-cancel',
  'search',
  'action',
  'batch-action',
  'load-more'
])

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  currentPage.value = newVal
})

watch(() => props.pageSize, (newVal) => {
  localPageSize.value = newVal
})

// 响应式数据
const tableRef = ref(null)
const searchQuery = ref('')
const selectedRows = ref([])
// 内部当前页码
const currentPage = ref(props.modelValue)
// 内部每页大小
const localPageSize = ref(props.pageSize)

// 计算属性
const tableData = computed(() => {
  // 如果有搜索查询且没有自定义过滤函数，则进行简单的文本过滤
  if (searchQuery.value && !props.filterFunc) {
    const query = searchQuery.value.toLowerCase()
    return props.data.filter(item => {
      return Object.values(item).some(value => {
        if (value === null || value === undefined) return false
        return String(value).toLowerCase().includes(query)
      })
    })
  }
  
  // 如果有自定义过滤函数，则使用自定义过滤函数
  if (searchQuery.value && props.filterFunc) {
    return props.data.filter(item => props.filterFunc(item, searchQuery.value))
  }
  
  return props.data
})

const totalPages = computed(() => {
  if (!props.showPagination) return 1
  return Math.ceil(props.total / props.pageSize)
})

// 监听搜索查询变化
const handleSearch = () => {
  emit('search', searchQuery.value)
}

// 处理行选择
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

// 清空选择
const clearSelection = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

// 设置选中行
const setSelection = (rows) => {
  if (tableRef.value && rows && rows.length > 0) {
    rows.forEach(row => {
      tableRef.value.toggleRowSelection(row, true)
    })
  }
}

// 处理操作按钮点击
const handleAction = (action, scope) => {
  emit('action', action, scope.row, scope)
}

// 处理批量操作按钮点击
const handleBatchAction = (action) => {
  emit('batch-action', action, selectedRows.value)
}

// 加载更多
const loadMore = () => {
  if (!props.loading && props.hasMore) {
    emit('load-more')
  }
}

// 处理当前页码变化
const handleCurrentChange = (currentRow) => {
  if (typeof currentRow === 'number') {
    // 处理分页组件的页码变化
    currentPage.value = currentRow
    emit('update:modelValue', currentRow)
  }
  emit('current-change', currentRow)
}

const handleSelect = (selection, row) => {
  emit('select', selection, row)
}

const handleSelectAll = (selection) => {
  emit('select-all', selection)
}

const handleCellMouseEnter = (row, column, cell, event) => {
  emit('cell-mouse-enter', row, column, cell, event)
}

const handleCellMouseLeave = (row, column, cell, event) => {
  emit('cell-mouse-leave', row, column, cell, event)
}

const handleCellClick = (row, column, cell, event) => {
  emit('cell-click', row, column, cell, event)
}

const handleCellDblclick = (row, column, cell, event) => {
  emit('cell-dblclick', row, column, cell, event)
}

const handleRowClick = (row, column, event) => {
  emit('row-click', row, column, event)
}

const handleRowDblclick = (row, column, event) => {
  emit('row-dblclick', row, column, event)
}

const handleHeaderClick = (column, event) => {
  emit('header-click', column, event)
}

const handleHeaderContextmenu = (column, event) => {
  emit('header-contextmenu', column, event)
}

const handleRowContextmenu = (row, column, event) => {
  emit('row-contextmenu', row, column, event)
}

const handleSortChange = (column, prop, order) => {
  emit('sort-change', column, prop, order)
}

const handleFilterChange = (filters) => {
  emit('filter-change', filters)
}

const handleExpandChange = (row, expandedRows) => {
  emit('expand-change', row, expandedRows)
}

const handleSelectCancel = (selection, row) => {
  emit('select-cancel', selection, row)
}

const handleSelectAllCancel = (selection) => {
  emit('select-all-cancel', selection)
}

const handleSizeChange = (size) => {
  localPageSize.value = size
  emit('update:pageSize', size)
}

// 暴露方法给父组件
defineExpose({
  clearSelection,
  setSelection,
  tableRef,
  selectedRows
})
</script>

<style scoped>
.advanced-table {
  width: 100%;
  display: flex;
  flex-direction: column;
}

/* 工具栏样式 */
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-search {
  flex: 1;
  max-width: 300px;
}

.toolbar-batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-info {
  font-size: 14px;
  color: var(--text-color-secondary);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 分页样式 */
.table-pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 0;
  gap: 16px;
}

/* 加载更多样式 */
.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .toolbar-search {
    max-width: none;
  }
  
  .table-pagination {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .toolbar-batch-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
}
</style>